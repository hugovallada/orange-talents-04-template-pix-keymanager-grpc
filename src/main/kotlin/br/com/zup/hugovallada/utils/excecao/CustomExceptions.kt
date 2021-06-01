package br.com.zup.hugovallada.utils.excecao

class ClientNotFoundException(message: String) : RuntimeException(message)
class PixKeyNotFoundException(message: String) : RuntimeException(message)
class ExistingPixKeyException(message: String) : RuntimeException(message)
class PermissionDeniedException(message: String) : RuntimeException(message)