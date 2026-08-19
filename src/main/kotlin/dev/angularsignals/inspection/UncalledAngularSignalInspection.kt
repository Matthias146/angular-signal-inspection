
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
import com.intellij.psi.util.parentOfType
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.Angular2Interpolation


class UncalledAngularSignalInspection : JSInspection() {


    override fun createVisitor(
        holder: ProblemsHolder,
        session: LocalInspectionToolSession,
    ): PsiElementVisitor {

        println("INSPECTION START: ${holder.file.name}")


        if (!Angular2LangUtil.isAngular2Context(holder.file)) {
            println("NOT ANGULAR CONTEXT")
            return PsiElementVisitor.EMPTY_VISITOR
        }


        return object : Angular2ElementVisitor() {


            override fun visitAngular2Interpolation(
                interpolation: Angular2Interpolation,
            ) {

                println("INTERPOLATION: ${interpolation.text}")


                val expression = interpolation.expression ?: return

                inspectExpression(
                    expression,
                    holder
                )
            }



            override fun visitAngular2BlockParameter(
                parameter: Angular2BlockParameter,
            ) {

                println("BLOCK PARAMETER: ${parameter.text}")


                val expression = parameter.expression ?: return

                inspectExpression(
                    expression,
                    holder
                )
            }
        }
    }



    private fun inspectExpression(
        expression: JSExpression,
        holder: ProblemsHolder,
    ) {

        expression.accept(
            object : PsiRecursiveElementVisitor() {


                override fun visitElement(element: PsiElement) {


                    if (element is JSReferenceExpression) {

                        inspectReference(
                            element,
                            holder
                        )
                    }


                    super.visitElement(element)
                }
            }
        )
    }



    private fun inspectReference(
        reference: JSReferenceExpression,
        holder: ProblemsHolder,
    ) {

        println("CHECK REF: ${reference.text}")

        // normale Aufrufe ignorieren:
        if (reference.parent is JSCallExpression) {
            println("SKIP CALL")
            return
        }


        // Angular @for / @if / @switch expressions
        // z.B. products() in @for
        val parentText = reference.parent.text

        if (parentText.startsWith("${reference.text}(")) {
            println("SKIP ANGULAR BLOCK CALL")
            return
        }


        val resolved = reference.resolve()

        println("RESOLVED: $resolved")


        val declarationText =
            resolved?.text ?: return

        println("DECLARATION TEXT: $declarationText")


        val isSignal = declarationText.contains(
            "signal("
        ) ||
                declarationText.contains(
                    "toSignal("
                )


        println("IS SIGNAL SELF: $isSignal")


        if (!isSignal) {
            return
        }


        println("REGISTER: ${reference.text}")


        holder.registerProblem(
            reference,
            "Angular signal should be invoked as ${reference.text}()",
        )
    }
}