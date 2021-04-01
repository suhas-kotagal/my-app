package com.logitech.integration.test.camera

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.MediaCodec
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Range
import android.view.*
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.logitech.integration.test.R
import com.logitech.integration.test.camera.model.CameraResults
import com.logitech.integration.test.camera.model.CameraSettings
import com.logitech.integration.test.camera.model.FocusViewModel
import com.logitech.integration.test.databinding.FragmentCameraBinding
import com.logitech.integration.test.views.FpsSurfaceView
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

const val ANIMATION_SLOW_MILLIS = 100L
const val MIN_REQUIRED_RECORDING_TIME_MILLIS: Long = 1000L

/**
 * Adapted from https://github.com/android/camera-samples.git
 */
class CameraFragment : Fragment() {
    val logger = LoggerFactory.getLogger(this.javaClass.name)

    interface OnCameraData{
        fun onRecordComplete(results: CameraResults)
    }
    /** Host's navigation controller */
    private val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    private val cameraManager: CameraManager by lazy {
        val context = requireContext().applicationContext
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    /** [CameraCharacteristics] corresponding to the provided Camera ID */
    private val characteristics: CameraCharacteristics by lazy {
        cameraManager.getCameraCharacteristics(cameraId)

    }

    private val cameraId : String by lazy {
        cameraManager.cameraIdList.first()
        //"2" Camera 2 is the view finder
    }

    /**
     * Setup a persistent [Surface] for the recorder so we can use it as an output target for the
     * camera session without preparing the recorder
     */
    private val recorderSurface: Surface by lazy {

        // Get a persistent Surface from MediaCodec, don't forget to release when done
        val surface = MediaCodec.createPersistentInputSurface()

        // Prepare and release a dummy MediaRecorder with our new surface
        // Required to allocate an appropriately sized buffer before passing the Surface as the
        //  output target to the capture session
        createRecorder(surface).apply {
            prepare()
            release()
        }

        surface
    }

    /** Saves the video recording */
    private val recorder: MediaRecorder by lazy { createRecorder(recorderSurface) }

    /** [HandlerThread] where all camera operations run */
    private val cameraThread = HandlerThread("CameraThread").apply { start() }

    /** [Handler] corresponding to [cameraThread] */
    private val cameraHandler = Handler(cameraThread.looper)

    /** Where the camera preview is displayed */
    private lateinit var fpsSurfaceView: FpsSurfaceView

    /** Overlay on top of the camera preview */
    private lateinit var overlay: View

    /** Captures frames from a [CameraDevice] for our video recording */
    private lateinit var session: CameraCaptureSession

    /** The [CameraDevice] that will be opened in this fragment */
    private lateinit var camera: CameraDevice

    var binding : FragmentCameraBinding? = null

    private var useRecorder = false

    lateinit var captureRequestBuilder : CaptureRequest.Builder

    lateinit var cameraResultsListener : OnCameraData

    private val focusViewModel : FocusViewModel by lazy {
        ViewModelProvider(requireActivity()).get(FocusViewModel::class.java)
    }

    /** Requests used for preview and recording in the [CameraCaptureSession] */
    private val recordRequest: CaptureRequest by lazy {
        // Capture request holds references to target surfaces

        val cameraSettings = requireActivity().intent.extras?.get(CameraSettings.EXTRA_NAME) as CameraSettings
        session.device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            // Add the preview and recording surface targets
            addTarget(fpsSurfaceView.holder.surface)
            addTarget(recorderSurface)

            // Sets user requested FPS for all targets
            set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(cameraSettings.fps, cameraSettings.fps))
        }.build()
    }

    private var recordingStartMillis: Long = 0L

    private val captureCallback = object : CaptureCallback(){
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            processCaptureResults(result)
        }
    }


    fun processCaptureResults(results: CaptureResult){
        when (results.get(CaptureResult.CONTROL_AF_STATE)){
            CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED -> {
                captureRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_TRIGGER,
                    CaptureRequest.CONTROL_AF_TRIGGER_IDLE
                )
                updateSessionPreview()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cameraResultsListener = context as OnCameraData
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)
        binding?.setLifecycleOwner(requireActivity())
        binding?.viewmodel = focusViewModel
        return binding?.root
    }

    fun setupObservers() {
        focusViewModel.autoFocus.observe(requireActivity(),
            Observer {
                if (::captureRequestBuilder.isInitialized) {

                    if (it) {
                        captureRequestBuilder.set(
                            CaptureRequest.CONTROL_AF_MODE,
                            CameraMetadata.CONTROL_AF_MODE_AUTO
                        )
                        captureRequestBuilder.set(
                            CaptureRequest.CONTROL_AF_TRIGGER,
                            CameraMetadata.CONTROL_AF_TRIGGER_START
                        )

                    } else {
                        captureRequestBuilder.set(
                            CaptureRequest.CONTROL_AF_MODE,
                            CameraMetadata.CONTROL_AF_MODE_OFF
                        )
                    }
                    updateSessionPreview()
                }

            }
        )
        focusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, value: Int, fromUser: Boolean) {
                if (fromUser){
                    captureRequestBuilder.set(
                        CaptureRequest.LENS_FOCUS_DISTANCE,
                        mapFocusRangeToCamera2FocusRange(value)
                    )
                    updateSessionPreview()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        focusViewModel.camera2InputFocusValue.observe(requireActivity(),
            Observer {value ->
                if (::captureRequestBuilder.isInitialized) {
                    captureRequestBuilder.set(
                        CaptureRequest.LENS_FOCUS_DISTANCE,
                        mapFocusRangeToCamera2FocusRange(value)
                    )
                    updateSessionPreview()
                }

            }
        )
    }

    /** Map the camera2 focus range to the device focus range as reported by the HAL */
    fun mapFocusRangeToCamera2FocusRange(value: Int) : Float{
        return ((value - focusViewModel.focusMin.value!!) / (focusViewModel.focusMax.value!! - focusViewModel.focusMin.value!!.toFloat()) ) * 10.0f
    }

    fun updateSessionPreview(){
        session.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, cameraHandler)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        overlay = view.findViewById(R.id.overlay)
        fpsSurfaceView = view.findViewById(R.id.fps_surface_view)

        fpsSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) = Unit
            override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int) = Unit

            override fun surfaceCreated(holder: SurfaceHolder) {
                // To ensure that size is set, initialize camera in the view's thread
                fpsSurfaceView.post { initializeCamera() }
            }
        })
        setupObservers()
        binding?.executePendingBindings()
    }

    fun getFps() : Double = fpsSurfaceView.getFps()



    /** Creates a [MediaRecorder] instance using the provided [Surface] as input */
    private fun createRecorder(surface: Surface) = MediaRecorder().apply {
        val cameraSettings = requireActivity().intent.extras?.get(CameraSettings.EXTRA_NAME) as CameraSettings
        requireNotNull(cameraSettings)
        requireNotNull(cameraSettings.cameraRecordOptions)
        logger.info("-----------${cameraSettings}")
        logger.info("-----------Record file= ${cameraSettings.cameraRecordOptions.outputFileName}")
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setVideoSource(MediaRecorder.VideoSource.SURFACE)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setOutputFile(cameraSettings.cameraRecordOptions.outputFileName)
        setVideoEncodingBitRate(cameraSettings.cameraRecordOptions.videoBitRate)
        setVideoFrameRate(cameraSettings.fps)
        setVideoSize(cameraSettings.videoSize.x, cameraSettings.videoSize.y)
        setVideoEncoder(cameraSettings.cameraRecordOptions.videoEncoding)
        setAudioEncoder(cameraSettings.cameraRecordOptions.audioEncoding)
        setInputSurface(surface)
    }


    fun createVideoFile(context: Context, extension: String): File {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
        return File(context.filesDir, "VID_${sdf.format(Date())}.$extension")
    }

    /** Requests used for preview only in the [CameraCaptureSession] */
    fun createDefaultCaptureRequest(cameraId: String, autoFocus : Boolean = true) {
        // Capture request holds references to target surfaces
        val captureBuilder =  session.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

        // Add the preview surface target
        captureBuilder.addTarget(fpsSurfaceView.holder.surface)
        if (!autoFocus) {
            captureBuilder.setPhysicalCameraKey(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF, cameraId)
        }
        this.captureRequestBuilder = captureBuilder

    }
    /**
     * Begin all camera operations in a coroutine in the main thread. This function:
     * - Opens the camera
     * - Configures the camera session
     * - Starts the preview by dispatching a repeating request
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeCamera() = lifecycleScope.launch(Dispatchers.Main) {
        val cameraSettings = requireActivity().intent.extras?.get(CameraSettings.EXTRA_NAME) as CameraSettings?
        useRecorder = cameraSettings?.cameraRecordOptions != null
        // Open the selected camera
        camera = openCamera(cameraManager, cameraId, cameraHandler)

        cameraManager.cameraIdList.forEach { println("Cameria ID  = ($it)") }
        //val characteristics = cameraManager.getCameraCharacteristics("0")
        //val allCapabilities = characteristics.get((CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES))

        // Creates list of Surfaces where the camera will output frames
        val targets = mutableListOf(fpsSurfaceView.holder.surface)
        if (useRecorder){
            targets.add(recorderSurface)
        }

        // Start a capture session using our open camera and list of Surfaces where frames will go
        session = createCaptureSession(camera, targets, cameraHandler)
        createDefaultCaptureRequest(cameraId)
        // Sends the capture request as frequently as possible until the session is torn down or
        //  session.stopRepeating() is called
        session.setRepeatingRequest(captureRequestBuilder.build() , captureCallback, cameraHandler)

        // React to user touching the capture button
        capture_button.setOnCheckedChangeListener { _ , isChecked ->
            when (isChecked) {

                true -> lifecycleScope.launch(Dispatchers.IO) {

                    // Prevents screen rotation during the video recording
                    requireActivity().requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_LOCKED

                    if (useRecorder) {
                        // Start recording repeating requests, which will stop the ongoing preview
                        //  repeating requests without having to explicitly call `session.stopRepeating`
                        session.setRepeatingRequest(recordRequest, null, cameraHandler)

                        // Finalizes recorder setup and starts recording
                        recorder.apply {
                            prepare()
                            start()
                        }
                    }
                    recordingStartMillis = System.currentTimeMillis()
                    logger.debug("Recording started")

                    // Starts recording animation
                    //overlay.post(animationTask)
                }

                false -> lifecycleScope.launch(Dispatchers.IO) {

                    // Unlocks screen rotation after recording finished
                    requireActivity().requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                    // Requires recording of at least MIN_REQUIRED_RECORDING_TIME_MILLIS
                    val elapsedTimeMillis = System.currentTimeMillis() - recordingStartMillis
                    if (elapsedTimeMillis < MIN_REQUIRED_RECORDING_TIME_MILLIS) {
                        delay(MIN_REQUIRED_RECORDING_TIME_MILLIS - elapsedTimeMillis)
                    }
                    if (useRecorder) {
                        recorder.stop()
                    }

                    cameraResultsListener.onRecordComplete(CameraResults(getFps()))
                    // Finishes our current camera screen
                    delay(ANIMATION_SLOW_MILLIS)
                    navController.popBackStack()

                }
            }
        }
    }

    /** Opens the camera and returns the opened device (as the result of the suspend coroutine) */
    @SuppressLint("MissingPermission")
    private suspend fun openCamera(manager: CameraManager, cameraId: String, handler: Handler? = null):
            CameraDevice = suspendCancellableCoroutine { cont ->

        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) = cont.resume(device)

            override fun onDisconnected(device: CameraDevice) {
                logger.debug( "Camera $cameraId has been disconnected")
                requireActivity().finish()
            }

            override fun onError(device: CameraDevice, error: Int) {
                val msg = when(error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                logger.error(exc.message, exc)
                if (cont.isActive) cont.resumeWithException(exc)
            }
        }, handler)
    }

    /**
     * Creates a [CameraCaptureSession] and returns the configured session (as the result of the
     * suspend coroutine)
     */
    private suspend fun createCaptureSession(
            device: CameraDevice,
            targets: List<Surface>,
            handler: Handler? = null
    ): CameraCaptureSession = suspendCoroutine { cont ->
        // Creates a capture session using the predefined targets, and defines a session state
        // callback which resumes the coroutine once the session is configured
        device.createCaptureSession(targets, object: CameraCaptureSession.StateCallback() {

            override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

            override fun onConfigureFailed(session: CameraCaptureSession) {
                val exc = RuntimeException("Camera ${device.id} session configuration failed")
                logger.error(exc.message, exc)
                cont.resumeWithException(exc)
            }
        }, handler)
    }

    override fun onStop() {
        super.onStop()
        try {
            camera.close()
        } catch (exc: Throwable) {
            logger.error("Error closing camera", exc)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraThread.quitSafely()
        if (useRecorder) {
            recorder.release()
            recorderSurface.release()
        }

    }
}
