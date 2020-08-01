package genogram

internal actual fun nativeLogInfo(className: String, message: String) {
    println("$className : $message")
}