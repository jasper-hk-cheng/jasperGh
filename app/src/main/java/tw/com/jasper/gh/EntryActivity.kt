package tw.com.jasper.gh

import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import tw.com.game.hours.lab.databinding.ActivityEntryBinding
import tw.com.game.hours.lab.databinding.ActivityMainBinding
import tw.com.jasper.gh.common.JasperGhActivity

class EntryActivity : JasperGhActivity<ActivityEntryBinding>() {

    override fun inflateViewBinding(): ActivityEntryBinding = ActivityEntryBinding.inflate(layoutInflater)

    override fun installView(viewBinding: ActivityEntryBinding) {
        with(viewBinding){
            // btnAdd.setOnClickListener {
            //     Timber.i("btn add click")
            // }
        }
    }

    override fun initView(viewBinding: ActivityEntryBinding) {
        dispatchTakePictureIntent()
    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        // Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
        //     // takePictureIntent.resolveActivity(packageManager)?.also {
        //         startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        //     // }
        // }
        val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            getViewBinding().ivThumbnail.setImageBitmap(imageBitmap)
        }
    }

}

// TODO 可能只對tab layout and bottomNavigationView有作用
// val badgeDrawable = BadgeDrawable.create(this@MainActivity).apply{
//     number= 3
//     isVisible=true
// }
// BadgeUtils.attachBadgeDrawable(badgeDrawable, btnAdd)

/* ---
setting
set listener
bind "use case" to lifecycle:ProcessCameraProvider.bindToLifecycle()
unbind "use case" from lifecycle: ProcessCameraProvider.unbindAll()

---
two ways:

CameraController
CameraProvider

---
CameraController - LifecycleCameraController

default use case:
Preview, ImageCapture, ImageAnalysis

toggle use case:
CameraController.setEnabledUseCases()

example code:
val previewView = viewBinding.previewView TODO xml?
val cameraController = LifeCameraController(context).apply{
    bindToLifecycle(this@Activity)
    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
}
previewView.controller = cameraController
---
CameraProvider

set use case:
set()
build()
CameraProvider.bindToLifecycle()

example code:
val preview = Preview.Builder().build()
// val viewFinder: PreviewView = findViewById(R.id.previewView)
val previewView = viewBinding.previewView
val camera=cameraProvider.bindToLifecycle(this@Activity, cameraSelector, preview)
preview.setSurfaceProvider(previewView.surfaceProvider)
---
CameraX customized Lifecycle example code:

class CustomLifecycle:LifeCycleOwner{
    private val lifecycleRegistry:LifecycleRegistry

    init{
        lifecycleRegistry = LifecycleRegistry(this).apply{
            markState(Lifecycle.State.CREATED)
        }
    }

    // manual control lifecycle
    fun doOnResume(){
        lifecycleRegistry.markState(Lifecycle.State.RESUME)
    }

    override fun getLifecycle():Lifecycle = this.lifecycleRegistry
}
---
merge use case:

private lateinit var imageCapture:ImageCapture

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

    cameraProviderFuture.addListener(Runnable{

        val preview = Preview.Builder().build()

        val imageCapture=ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()

        val cameraSelector=CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

        val cameraProvider = cameraProviderFuture.get()
        val camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageCapture)

        preview.setSurfaceProvider(previewView.getSurfaceProvider())

    }, ContextCompat.getMainExecutor(this))
}
---
check hardware support level:

@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
fun isBackCameraLevel3Device(cameraProvider:ProcessCameraProvider):Boolean{
    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
        val result= CameraSelector.DEFAULT_BACK_CAMERA.filter(cameraProvider.availableCameraInfos).firstOrNull()?.let{
            Camera2CameraInfo.from(it) // it -> is CameraInfo class type
        }?.getCameraCharacteristics(CameraCharacteristic.INFO_SUPPORTED_HARDWARE_LEVEL) == CameraCharacteristic.INFO_SUPPORTED_HARDWARE_LEVEL_3
        return result
    }else return false
}
---
cameraX and camera2

important class:
Camera2CameraInfo
CameraCharacteristic

Camera2CameraControl -> CaptureRequest
Camera2Interop.Extender -> set CaptureRequest(like Camera2CameraControl)

example code:

val videoCallStreamId:Long = CameraMetadata.SCALER_AVAILABLE_STREAM_USE_CASES_VIDEO_CALL.toLong()

val frontCameraInfo = cameraProvider.getAvailableCameraInfos().first{cameraInfo->
    val isVideoCallStreamingSupported = Camera2CameraInfo.from(cameraInfo)
        .getCameraCharacteristic(CameraCharacteristic.SCALER_AVAILABLE_STREAM_USE_CASES)?
        .contains(videoCallStreamId)
    val isFrontFacing =cameraInfo.getLensFacing() == CameraSelector.LENS_FACING_FRONT

    // return
    isVideoCallStreamingSupported && isFrontFacing
}
val cameraSelector = frontCameraInfo.cameraSelector

// preview
val previewBuilder:Preview.Builder = Preview.Builder()
    .setTargetAspectRatio(screenAspectRatio) // TODO screenAspectRatio 哪來的?
    .setTargetRotation(rotation) // TODO rotation 哪來的?
Camera2Interop.Extender(previewBuilder).setStreamUseCase(videoCallStreamId)
val preview = previewBuilder.build()

camera = cameraProvider.buildToLifecycle(this@Activity, cameraSelector, preview)
--- --- ---
[Setting]

ImageCapture:
ImageCapture.Builder()
    .setFlashMode(...)
    .setTargetAspectRatio(...)
    .build()
ImageCapture.takePicture()


ImageAnalysis.setTargetRotation()
Preview.PreviewOutput()



CameraXConfig(in Application):

class CameraApplication :Application(), CameraXConfig.Provider{
    override fun getCameraXConfig():CameraXConfig = CameraXConfig.Builder
        .fromConfig(Camera2Config.defaultConfig())
        .setMinimumLoggingLevel(Log.ERROR)
        .build()
}

class MainApplication:Application(), CameraXConfig.provider{
    override fun getCameraXConfig():CameraXConfig = CameraXConfig.Builder
        .formConfig(Camera2Config.defaultConfig())
        .setAvailableCamerasLimiter(CameraSelector.DEFAULT_BACK_CAMERA)
        .build()
}

background thread selection:
CameraXConfig.Builder.setCameraExecutor()
CameraXConfig.Builder.setScheduleHandler() TODO different from? 應該暫時用不到

rotation:
override fun onCreate(){
    val imageCapture = ImageCapture.Builder().build()

    val orientationEventLnr=object:OrientationEventListener(context){
        override fun onOrientationChanged(orientation:Int){
            val rotation:int = when(orientation){
                in 45..134 -> Surface.ROTATION_270
                in 135..224 -> Surface.ROTATION_180
                in 225..314 -> Surface.ROTATION_90
                else -> Surface.RATATION_0
            }
            imageCapture.targetRotation = rotation
       }
    }
    orientationEventLnr.enable()
}

viewPort:
val viewPort = ViewPort.Builder(Rational(width, height), display.rotation).build()
val useCaseGroup = UseCaseGroup.Builder()
    .addUseCase(preview)
    .addUseCase(imageAnalysis)
    .addUseCase(imageCapture)
    .setViewPort(viewPort)
    .build()

cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)

get view port (simple way) by preview view:
val viewPort =findViewById<PreviewView>(R.id.preview_view).viewPort

camera selection:

select manual -
CameraSelector.default_front_camera
CameraSelector.default_back_camera
CameraSelector.Builder.addCameraFilter() by CameraCharacteristic

get all camera id
CameraManager.getCameraIdList()

外接鏡頭
be sure "PackageManager.FEATURE_CAMERA_EXTERNAL" is active

example code:

1.find the best or external one

fun selectExternalOrBestCamera(cameraProvider:ProcessCameraProvider):CameraSelector?{
    val cam2Infos = cameraProvider.availableCameraInfos.map{
        Camera2CameraInfos.from(it)
    }.sortedByDescending{
        it.getCharacteristic(CameraCharacteristic.INFO_SUPPORT_HARDWARE_LEVEL)
    }
    return when{
        cam2Infos.isNotEmpty->{
            CameraSelector.Builder().addCameraFilter{
                it.filter{ camInfo-> TODO 這裡兩層filter不太懂
                    val thisCamId=Camera2CameraInfo.from(camInfo).cameraId
                    thisCamId == cam2Infos[0].cameraId
                }
            }
        }
        else -> null
    }
}
val selector=selectExternalOrBestCamera(processCameraProvider)
processCameraProvider.bindToLifecycle(this@Activity, selector,preview,... )

2.select multi-camera

val primary = ConcurrentCamera.SingleCameraConfig(
    primaryCameraSelector,
    useCaseGroup,
    lifecycleOwner
)
val secondary =ConcurrentCamera.SingleCameraConfig(
    secondaryCameraSelector,
    useCaseGroup,
    lifecycleOwner
)

val concurrentCamera=cameraProvider.bindToLifecycle(listOf(primary, secondary))
val primaryCamera =concurrentCamera.cameras[0]
val secondaryCamera =concurrentCamera.cameras[1]
---
導入預覽
https://developer.android.com/training/camerax/preview?hl=zh-tw
圖片拍攝
https://developer.android.com/training/camerax/take-photo?hl=zh-tw
Future Pattern
https://openhome.cc/zh-tw/pattern/thread/future/
---
TODO 下次起點：設定 - 相機解析度
https://developer.android.com/training/camerax/configuration?hl=zh-tw

TODO 使用 Life-Aware 元件處理生命週期
https://developer.android.com/topic/libraries/architecture/lifecycle?hl=zh-tw#implementing-lco
---
PreviewView

CameraXConfig.Provider -> optional

layout xml:

<FrameLayout
id container
    <androidx.camera.view.PreviewView
        id preview_view
    >
></FrameLayout>

request CameraProvider:

class MainActivity:AppCampatActivity(){
    private lateinit var cameraProviderFuture:ListenableFeature<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?){
        cameraProviderFuture=ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable{
            val cameraProvider = cameraProviderFuture.get()

            // 確認初始化成功
            bindPreview(cameraProvider)

        }, ContextCompat.getMainExecutor())
    }

    private fun bindPreview(cameraProvider:ProcessCameraProvider){

        val cameraSelector:CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

       val preview:Preview = Preview.Builder().build().apply{
            setSurfaceProvider(previewView.getSurfaceProvider())
            // optional
            implementationMode=PreviewView.ImplementationMode.COMPATIBLE
            scaleType=PreviewView.ScaleType.FIT_CENTER
       }
        val camera = cameraProvider.bindToLifecycle(this@Activity,cameraSelector,preview )
    }
}

ImageCapture

takePicture(Executor, OnImageCapturedCallback)
takePicture(OutputFileOptions, Executor, OnImageSavedCallback)

ImageCapture.Builder().setCaptureMode()
CAPTURE_MODE_MINIMIZE_LATENCY
CAPTURE_MODE_MAXIMIZE_QUALITY
CAPTURE_MODE_ZERO_SHOT_LAG - 實驗階段 先用 isZslSupported() 看硬體是否支援

ImageCapture.Builder().setFlashMode()
FLASH_MODE_OFF
FLASH_MODE_ON
FLASH_MODE_AUTO

// make camera be ready
val imageCapture=ImageCapture.Builder()
    .setTargetRotation(view.display.rotation)
    .build()
cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture, preview)
// trigger capture
val outputFileOptions=ImageCapture.OutputFileOptions.Builder(File(...)).build()
imageCapture.takePicture(outputFileOptions, cameraExecutor, object:ImageCapture.OnImageSavedCallback{
    override fun onError(imageCaptureException:ImageCaptureException){

    }
    override fun onImageSaved(outFileResults:ImageCapture.OutputFileResults){

    }
})

--- --- --- --- --- ---




 */