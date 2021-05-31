package br.com.zup.hugovallada.utils.validacao

import br.com.zup.hugovallada.TipoDeChave
import br.com.zup.hugovallada.pix.CadastraChavePixRequest
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import io.micronaut.validation.validator.constraints.EmailValidator
import java.lang.IllegalStateException
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.Email
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.reflect.KClass

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(
    val message: String = "Não é uma chave pix válida",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidPixKeyValidator : ConstraintValidator<ValidPixKey, CadastraChavePixRequest> {
    override fun isValid(
        value: CadastraChavePixRequest?,
        annotationMetadata: AnnotationValue<ValidPixKey>,
        context: ConstraintValidatorContext
    ): Boolean {
        if(value?.tipo == null) return false
        if(value.chave.isNullOrBlank() && value.tipo != TipoDeChave.CHAVE_ALEATORIA) return false

        when(value.tipo) {
            TipoDeChave.EMAIL -> {
                return value.chave!!.matches("[a-zA-Z0-9]+@[a-z]+\\.[a-zA-Z.]*".toRegex())
            }
            TipoDeChave.TELEFONE_CELULAR -> {
                return value.chave!!.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
            }
            TipoDeChave.CPF -> {
                return value.chave!!.matches("[0-9]+".toRegex())
            }
            TipoDeChave.DESCONHECIDO -> throw IllegalStateException("Não deveria ter acontecido isso")
            TipoDeChave.CHAVE_ALEATORIA -> return value.chave.isNullOrBlank()
        }

        return false
    }

}
