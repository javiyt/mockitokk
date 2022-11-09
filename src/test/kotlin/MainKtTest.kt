import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MainKtTest {

    @Test
    internal fun `it should remove the mockito imports`() {
        // TODO Add imports
        // import io.mockk.every
        // import io.mockk.mockk
        // import io.mockk.verify
        assertThat(
            """
                import com.nhaarman.mockitokotlin2.any
                import com.nhaarman.mockitokotlin2.given
                import com.nhaarman.mockitokotlin2.inOrder
                import com.nhaarman.mockitokotlin2.mock
                import com.nhaarman.mockitokotlin2.times
                import com.nhaarman.mockitokotlin2.verify
                import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
                import org.assertj.core.api.Assertions.assertThat
                import org.junit.jupiter.api.Nested
                import org.junit.jupiter.api.Test
                import java.util.UUID
            """.trimIndent().removeImport()
        ).isEqualTo(
            """
            import org.assertj.core.api.Assertions.assertThat
            import org.junit.jupiter.api.Nested
            import org.junit.jupiter.api.Test
            import java.util.UUID
            """.trimIndent()
        )
    }

    @Test
    internal fun `it should replace mock`() {
        assertThat("private val mockedClass = mock<MyClass>()".trimIndent().replaceMock())
            .isEqualTo("private val mockedClass = mockk<MyClass>()")
        // TODO handle more cases
        // private val mockedClass: MyClass = mock()
        // @Mock
        // private lateinit var mockedClass: MyClass
    }

    @Test
    internal fun `it should replace given`() {
        assertThat(
            """
            given(myMockedClass.findById(any())).willReturn(expectedValue)            
            """.trimIndent().replaceGivenReturns()
        ).isEqualTo(
            """
            every { myMockedClass.findById(any()) } returns expectedValue            
            """.trimIndent().replaceGivenReturns()
        )

        // TODO more cases to handle:
        //         given(
        //            myMockedClass.findById(
        //                param1,
        //                param2
        //            )
        //        ).willReturn(returnedValue)
        //
        //         given(myMockedClass.findById(param1)).willReturn(
        //            someFunction(
        //                listOf(
        //                    returnedValue
        //                )
        //            )
        //        )
    }

    @Test
    internal fun `it should replace verify`() {
        assertThat(
            """
                assertThat(result).isEqualTo(expectedResult)
    
                verify(myMockedClass, times(1))
                    .findById(id)
            """.trimIndent().replaceVerify()
        ).isEqualTo(
            """
                assertThat(result).isEqualTo(expectedResult)
    
                verify(exactly = 1) { myMockedClass.findById(id) }
            """.trimIndent()
        )

        // TODO more cases to handle:
        // verify(myMockedClass, times(1)).execute(
        //             param1,
        //            param2
        //        )
        //
        // verify(myMockedClass, never()).execute(param1, param2)
    }
}