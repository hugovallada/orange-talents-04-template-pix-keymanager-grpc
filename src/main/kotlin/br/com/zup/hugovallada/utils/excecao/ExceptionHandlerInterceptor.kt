package br.com.zup.hugovallada.utils.excecao

import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandlerInterceptor(@Inject private val resolver: ExceptionHandlerResolver) : MethodInterceptor<BindableService, Any?> {

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {
        return try {
            context.proceed()
        } catch (e: Exception){
            val status = resolver.resolve(e).handle(e)

            GrpcEndpointArguments(context).response()
                .onError(status.asRuntimeException())

            null
        }
    }
}

private class GrpcEndpointArguments(val context: MethodInvocationContext<BindableService, Any?>){
    fun response(): StreamObserver<*> {
        return context.parameterValues[1] as StreamObserver<*>
    }
}