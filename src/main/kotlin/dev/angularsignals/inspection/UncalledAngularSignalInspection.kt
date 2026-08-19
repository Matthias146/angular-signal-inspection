package dev.angularsignals.inspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.inspections.JSInspection
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiRecursiveElementVisitor
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.Angular2Interpolation
import org.angular2.signals.Angular2SignalUtils

class UncalledAngularSignalInspection : JSInspection() {

    override fun createVisitor(
        holder: ProblemsHolder,
        session: LocalInspectionToolSession,
    ): PsiElementVisitor {
        if (!Angular2LangUtil.isAngular2Context(holder.file)) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        return object : Angular2ElementVisitor() {

            override fun visitAngular2Interpolation(
                interpolation: Angular2Interpolation,
            ) {
                val expression = interpolation.expression ?: return
                inspectExpression(expression, holder)
            }

            override fun visitAngular2BlockParameter(
                parameter: Angular2BlockParameter,
            ) {
                val expression = parameter.expression ?: return
                inspectExpression(expression, holder)
            }
        }
    }

    private fun inspectExpression(
        expression: JSExpression,
        holder: ProblemsHolder,
    ) {
        expression.accept(object : PsiRecursiveElementVisitor() {

            override fun visitElement(element: PsiElement) {
                if (element is JSReferenceExpression) {
                    inspectReference(element, holder)
                }

                super.visitElement(element)
            }
        })
    }

    private fun inspectReference(
        reference: JSReferenceExpression,
        holder: ProblemsHolder,
    ) {
        // Correctly invoked signal: products()
        if (reference.parent is JSCallExpression) {
            return
        }

        val isSignal = Angular2SignalUtils.isSignal(
            reference,
            reference,
            false,
        )

        if (!isSignal) {
            return
        }

        holder.registerProblem(
            reference,
            "Angular signal should be invoked",
            InvokeSignalQuickFix(),
        )
    }
}