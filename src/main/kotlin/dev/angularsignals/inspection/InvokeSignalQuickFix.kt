package dev.angularsignals.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.project.Project

class InvokeSignalQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Invoke Angular signal"
    }

    override fun applyFix(
        project: Project,
        descriptor: ProblemDescriptor,
    ) {
        val expression =
            descriptor.psiElement as? JSReferenceExpression ?: return

        val newNode = JSChangeUtil.createExpressionWithContext(
            "${expression.text}()",
            expression,
        ) ?: return

        val newExpression =
            newNode.psi as? JSExpression ?: return

        expression.replace(newExpression)
    }
}