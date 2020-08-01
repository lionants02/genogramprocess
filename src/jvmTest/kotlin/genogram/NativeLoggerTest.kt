package genogram

import org.amshove.kluent.`should be equal to`
import org.junit.Test

class NativeLoggerTest {

    @Test
    fun info() {
        this::class.simpleName!! `should be equal to` "NativeLoggerTest"
    }
}