package nikmax.gallery.gallery.core.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException



@RunWith(AndroidJUnit4::class)
class MediaItemUiInstrumentedTest {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    private fun grantManageExternalStoragePermission() {
        try {
            val packageName: String = appContext.packageName
            val command = "pm grant $packageName android.permission.MANAGE_EXTERNAL_STORAGE"
            Runtime.getRuntime().exec(command)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Before
    fun setup() {
        grantManageExternalStoragePermission()
    }

    @Test
    fun protectedItemCheckReturnsCorrectResult() {
        val storage = MediaItemUI.Album("/storage/emulated/0")
        val realResultForStorage = storage.isVolume
        val desiredResultForStorage = true
        assert(realResultForStorage == desiredResultForStorage)

        val sdcard = MediaItemUI.Album("/storage/ABCD-1234")
        val realResultForSdcard = sdcard.isVolume
        val desiredResultForSdcard = true
        assert(realResultForSdcard == desiredResultForSdcard)
    }
}
