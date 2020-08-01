package genogram

internal actual fun nativeLogInfo(className: String, message: String) {
    console.log("$className : $message")
}