package tw.com.jasper.gh

import android.app.Application
import timber.log.Timber
import tw.com.game.hours.lab.BuildConfig

class JasperGhApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(JasperDebugTree())
        }
    }
}

class JasperDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String =
        "(${element.fileName}:${element.lineNumber})#${element.methodName}"
}
