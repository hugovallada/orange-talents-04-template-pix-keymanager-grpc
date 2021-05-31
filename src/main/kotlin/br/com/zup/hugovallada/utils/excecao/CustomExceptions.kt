package br.com.zup.hugovallada.utils.excecao

import java.lang.RuntimeException

class ClientNotFoundException(message: String): RuntimeException(message)
class ExistingPixKeyException(message: String): RuntimeException(message)