package org.javarosa.kotlintest

import junit.framework.TestCase
import org.javarosa.kotlintest.ThisOrThat.THAT
import org.javarosa.kotlintest.ThisOrThat.THIS
import org.junit.Test

class KotlinGradleSetupTest {
    @Test
    fun `can compile Kotlin in directory`() {
        TestCase.assertNotSame(THIS, THAT)
        TestCase.assertSame(THIS, THIS)
    }
}
