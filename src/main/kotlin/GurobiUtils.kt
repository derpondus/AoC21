import com.gurobi.gurobi.GRB
import com.gurobi.gurobi.GRBEnv
import com.gurobi.gurobi.GRBLinExpr
import com.gurobi.gurobi.GRBModel
import com.gurobi.gurobi.GRBVar

val AOC_GUROBI_ENV by lazy { GRBEnv("LOG_AOC_GUROBI").apply {
    set(GRB.IntParam.OutputFlag, 0)
} }

// Constraint building (Python style with operator overloading)

// GRBVar
operator fun GRBVar.unaryPlus(): GRBLinExpr = this * 1.0
operator fun GRBVar.unaryMinus(): GRBLinExpr = this * -1.0

// GRBVar x Number
operator fun GRBVar.plus(other: Number): GRBLinExpr {
    val expr = GRBLinExpr()
    expr.addTerm(1.0, this)
    expr.addConstant(other.toDouble())
    return expr
}
operator fun GRBVar.minus(other: Number): GRBLinExpr = this + (-other.toDouble())

operator fun GRBVar.times(other: Number): GRBLinExpr {
    val expr = GRBLinExpr()
    expr.addTerm(other.toDouble(), this)
    return expr
}
operator fun GRBVar.div(other: Number): GRBLinExpr = this * (1.0 / other.toDouble())

// Number x GurobiVar
operator fun Number.plus(other: GRBVar): GRBLinExpr = other + this
operator fun Number.minus(other: GRBVar): GRBLinExpr = (-other) + this
operator fun Number.times(other: GRBVar): GRBLinExpr = other * this
operator fun Number.div(other: GRBVar): GRBLinExpr {
    TODO("I dont want to have to think about this right now!")
}

// GRBVar x GRBVar
operator fun GRBVar.plus(other: GRBVar): GRBLinExpr {
    val expr = GRBLinExpr()
    expr.addTerm(1.0, this)
    expr.addTerm(1.0, other)
    return expr
}
operator fun GRBVar.minus(other: GRBVar): GRBLinExpr = this + (-other)
/* Not linear | operator fun GRBVar.times(other: GRBVar): GRBLinExpr */
/* Not linear | operator fun GRBVar.div(other: GRBVar): GRBLinExpr */


// GRBLinExpr
operator fun GRBLinExpr.unaryPlus(): GRBLinExpr = this
operator fun GRBLinExpr.unaryMinus(): GRBLinExpr = this * -1.0

// GRBLinExpr x Number
operator fun GRBLinExpr.plus(other: Number): GRBLinExpr = this.also { it.addConstant(other.toDouble()) }
operator fun GRBLinExpr.minus(other: Number): GRBLinExpr = this.also { it.addConstant(-other.toDouble()) }
operator fun GRBLinExpr.times(other: Number): GRBLinExpr {
    val expr = GRBLinExpr()
    expr.multAdd(other.toDouble(), this)
    return expr
}
operator fun GRBLinExpr.div(other: Number): GRBLinExpr = this * (1.0 / other.toDouble())

// Number x GRBLinExpr
operator fun Number.plus(other: GRBLinExpr): GRBLinExpr = other + this
operator fun Number.minus(other: GRBLinExpr): GRBLinExpr = other - this
operator fun Number.times(other: GRBLinExpr): GRBLinExpr = other * this
operator fun Number.div(other: GRBLinExpr): GRBLinExpr = other / this

// GRBLinExpr x GRBVar
operator fun GRBLinExpr.plus(other: GRBVar): GRBLinExpr = this.also { it.addTerm(1.0, other) }
operator fun GRBLinExpr.minus(other: GRBVar): GRBLinExpr = this.also { it.addTerm(-1.0, other) }

// GRBVar x GRBLinExpr
operator fun GRBVar.plus(other: GRBLinExpr): GRBLinExpr = other.also { it.addTerm(1.0, this) }
operator fun GRBVar.minus(other: GRBLinExpr): GRBLinExpr = other.also { it.addTerm(-1.0, this) }

// GRBLinExpr x GRBLinExpr
operator fun GRBLinExpr.plus(other: GRBLinExpr): GRBLinExpr = other.also { it.add(this) }
operator fun GRBLinExpr.minus(other: GRBLinExpr): GRBLinExpr = other.also { it.multAdd(-1.0, this) }


// Constraint Components
data class ConstraintComponents(
    val lhsExpr: GRBLinExpr,
    val sense: Char,
    val rhsExpr: GRBLinExpr,
)

// Number -> GRBLinExpr
fun Number.asGRBLinExpr(): GRBLinExpr {
    val expr = GRBLinExpr()
    expr.addConstant(toDouble())
    return expr
}

// GRBLinExpr x GRBLinExpr
infix fun GRBLinExpr.geq(other: GRBLinExpr) = ConstraintComponents(this, GRB.GREATER_EQUAL, other)
infix fun GRBLinExpr.eq(other: GRBLinExpr) = ConstraintComponents(this, GRB.EQUAL, other)
infix fun GRBLinExpr.leq(other: GRBLinExpr) = ConstraintComponents(this, GRB.LESS_EQUAL, other)

// GRBLinExpr x GRBVar
infix fun GRBLinExpr.geq(other: GRBVar) = this geq (+other)
infix fun GRBLinExpr.eq(other: GRBVar) = this eq (+other)
infix fun GRBLinExpr.leq(other: GRBVar) = this leq (+other)

// GRBVar x GRBLinExpr
infix fun GRBVar.geq(other: GRBLinExpr) = (+this) geq other
infix fun GRBVar.eq(other: GRBLinExpr) = (+this) eq other
infix fun GRBVar.leq(other: GRBLinExpr) = (+this) leq other

// GRBLinExpr x Number
infix fun GRBLinExpr.geq(other: Number) = this geq other.asGRBLinExpr()
infix fun GRBLinExpr.eq(other: Number) = this eq other.asGRBLinExpr()
infix fun GRBLinExpr.leq(other: Number) = this leq other.asGRBLinExpr()

// Number x GRBLinExpr
infix fun Number.geq(other: GRBLinExpr) = this.asGRBLinExpr() geq other
infix fun Number.eq(other: GRBLinExpr) = this.asGRBLinExpr() eq other
infix fun Number.leq(other: GRBLinExpr) = this.asGRBLinExpr() leq other

// GRBVar x GRBVar
infix fun GRBVar.geq(other: GRBVar) = (+this) geq other
infix fun GRBVar.eq(other: GRBVar) = (+this) eq other
infix fun GRBVar.leq(other: GRBVar) = (+this) leq other

// GRBVar x Number
infix fun GRBVar.geq(other: Number) = (+this) geq other
infix fun GRBVar.eq(other: Number) = (+this) eq other
infix fun GRBVar.leq(other: Number) = (+this) leq other

// Number x GRBVar
infix fun Number.geq(other: GRBVar) = this geq (+other)
infix fun Number.eq(other: GRBVar) = this eq (+other)
infix fun Number.leq(other: GRBVar) = this leq (+other)

// Number x Number
infix fun Number.geq(other: Number) = this geq other.asGRBLinExpr()
infix fun Number.eq(other: Number) = this eq other.asGRBLinExpr()
infix fun Number.leq(other: Number) = this leq other.asGRBLinExpr()


// Model
fun GRBModel.addConstr(components: ConstraintComponents, name: String) = addConstr(components.lhsExpr, components.sense, components.rhsExpr, name)


// Collection
fun <V> Collection<V>.grbSum(selector: (V) -> GRBLinExpr): GRBLinExpr = this
    .map { selector(it) }
    .reduce(GRBLinExpr::plus)
