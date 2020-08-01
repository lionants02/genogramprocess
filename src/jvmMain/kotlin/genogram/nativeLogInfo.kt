package genogram

import org.apache.logging.log4j.LogManager

internal actual fun nativeLogInfo(className: String, message: String) {
    LogManager.getLogger(className).info(message)
}