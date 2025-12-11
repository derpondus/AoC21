package year2025

import AOC_GUROBI_ENV
import addConstr
import com.gurobi.gurobi.GRB
import com.gurobi.gurobi.GRB.DoubleAttr
import com.gurobi.gurobi.GRBModel
import com.gurobi.gurobi.GRBVar
import eq
import grbSum
import plus
import times

data class IndicatorLightTarget(val shouldBeOn: Boolean)
data class Button(val attachedIds: List<Int>)
data class Machine(
    val indicatorLightTargets: List<IndicatorLightTarget>,
    val buttons: List<Button>,
    val joltageCounterTargets: List<Int>
)

fun day10(inputLines: List<String>) {
    val machines = inputLines.map {
        val parts = it.split(" ")
        Machine(
            parts.first().trim('[', ']').map { IndicatorLightTarget(it == '#') },
            parts.drop(1).dropLast(1).map { Button(it.trim('(', ')').split(',').map(String::toInt)) },
            parts.last().trim('{', '}').split(',').map(String::toInt)
        )
    }

    val (modelP1, x1) = buildModelP1(machines)
    modelP1.optimize()
    val p1PressCount = x1.values.flatMap { it.values }.count { it.get(DoubleAttr.X) >= 0.5 }
    println("[Part1] Number of Button-presses: $p1PressCount")
    // 475 (correct)

    val (modelP2, x2) = buildModelP2(machines)
    modelP2.optimize()
    val p2PressCount = x2.values.flatMap { it.values }.sumOf { it.get(DoubleAttr.X).toInt() }
    println("[Part2] Number of Button-presses: $p2PressCount")
    // 1148 (too low) (ich will garnicht wissen wie viele buttons mindestens einmal gedrückt werden :O )
    // 18273 (correct)
}

fun buildModelP1(machines: List<Machine>): Pair<GRBModel, Map<Int, Map<Int, GRBVar>>> {
    // === Gurobi model ===
    val model = GRBModel(AOC_GUROBI_ENV)

    // --- Variables ---
    // Button press variables: whether a button should be pressed (value 1) or not (value 0)
    val x = machines.withIndex().associate { (mId, m) ->
        mId to m.buttons.withIndex().associate { (bId, _) ->
            bId to model.addVar(0.0, 1.0, 1.0, GRB.BINARY, "x_machine_${mId}_button_${bId}")!!
        }
    }

    // Light cycle variables: how often each light get turned on AND off
    val y = machines.withIndex().associate { (mId, m) ->
        mId to m.indicatorLightTargets.withIndex().associate { (lId, _) ->
            lId to model.addVar(0.0, Double.MAX_VALUE, 0.0, GRB.INTEGER, "y_machine_${mId}_button_${lId}")!!
        }
    }

    model.update()  // update to make the variables known

    // --- Constraints ---
    // Lamps have to be in correct final state
    machines.withIndex().forEach { (mId, m) ->
        m.indicatorLightTargets.withIndex().forEach { (lId, l) ->
            val buttonVars = x[mId]!!
            val lhs = l.shouldBeOn.asInt() + buttonVars.entries.grbSum { (bId, it) -> (m.buttons[bId].attachedIds.contains(lId)).asInt() * it }
            model.addConstr(lhs eq (2 * y[mId]!![lId]!!), "state_machine_${mId}_light_${lId}")!!
        }
    }

    // --- Return the model ---
    model.update()
    return Pair(model, x)
}

fun buildModelP2(machines: List<Machine>): Pair<GRBModel, Map<Int, Map<Int, GRBVar>>> {
    // === Gurobi model ===
    val model = GRBModel(AOC_GUROBI_ENV)

    // --- Variables ---
    // Button press variables: how often a button should be pressed
    val x = machines.withIndex().associate { (mId, m) ->
        mId to m.buttons.withIndex().associate { (bId, _) ->
            bId to model.addVar(0.0, Double.MAX_VALUE, 1.0, GRB.INTEGER, "x_machine_${mId}_button_${bId}")!!
        }
    }

    model.update()  // update to make the variables known

    // --- Constraints ---
    // Counters have to be in correct final state
    machines.withIndex().forEach { (mId, m) ->
        m.joltageCounterTargets.withIndex().forEach { (jId, j) ->
            val buttonVars = x[mId]!!
            val lhs = buttonVars.entries.grbSum { (bId, it) -> (m.buttons[bId].attachedIds.contains(jId)).asInt() * it }
            model.addConstr(lhs eq j, "state_machine_${mId}_counter_${jId}")!!
        }
    }

    // --- Return the model ---
    model.update()
    return Pair(model, x)
}

fun Boolean.asInt() = if (this) 1 else 0
