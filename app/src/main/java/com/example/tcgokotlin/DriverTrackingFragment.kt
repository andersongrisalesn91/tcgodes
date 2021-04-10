package com.example.tcgokotlin

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.tcgokotlin.Helper.GPSTracker
import com.example.tcgokotlin.Helper.Tools
import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.ModulDriverTracking.EditDirectionDialogFragment
import com.example.tcgokotlin.ModulMain.MainActivity
import com.example.tcgokotlin.Sincronizar.CargarEstados
import com.example.tcgokotlin.data.model.*
import com.example.tcgokotlin.data.network.Repo
import com.example.tcgokotlin.domain.UseCase
import com.example.tcgokotlin.presentation.viewmodel.MainViewModel
import com.example.tcgokotlin.presentation.viewmodel.MainViewModelFactory
import com.example.tcgokotlin.sqliteDBHelper.Backups
import com.example.tcgokotlin.sqliteDBHelper.ConsultaGeneral
import com.example.tcgokotlin.sqliteDBHelper.FuncionesGenerales
import com.example.tcgokotlin.sqliteDBHelper.LoadTaskSqlite
import com.example.tcgokotlin.utils.Common
import com.example.tcgokotlin.utils.sharedPreferences.SesionManager
import com.firebase.geofire.GeoFire
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.Polyline
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import io.paperdb.Paper
import kotlinx.android.synthetic.main.fragment_driver_tracking.*
import kotlinx.android.synthetic.main.layout_info_task.*
import kotlinx.android.synthetic.main.layout_list_rutes.*
import org.json.JSONObject
import java.util.*
import java.util.Date
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class DriverTrackingFragment : Fragment(), OnMapReadyCallback, LocationListener {

    private lateinit var vwCurrent: View
    private lateinit var gpsInfo: GPSTracker
    private lateinit var mMap: GoogleMap
    private lateinit var mLocationRequest: LocationRequest
    private var ubicateTasks: LatLng? = null
    private var sesionManager: SesionManager? = null
    private var mediaPlayer: MediaPlayer? = null

    private val viewModelActivity by activityViewModels<MainViewModel> {
        MainViewModelFactory(UseCase(Repo()))
    }

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(),
            R.anim.rotate_open_anim)
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(),
            R.anim.rotate_close_anim)
    }
    private val fromRight: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(),
            R.anim.from_right_anim)
    }
    private val toRight: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(),
            R.anim.to_right_anim)
    }

    private var clicked = false
    private var startDir = ""
    var idtarea: String = ""
    var estadotask: String = ""
    private var inimap = ""
    var recEstado = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Common.ifJornada != 1) {
            Toast.makeText(requireActivity(),
                getString(R.string.must_init_forst_workday),
                Toast.LENGTH_SHORT).show()
            requireActivity().startActivity(Intent(requireActivity(), MainActivity::class.java))
        }
        arguments.let {
            if (arguments?.getDouble("latitude") != null && arguments?.getDouble("latitude") != 0.0) {
                ubicateTasks = LatLng(arguments?.getDouble("latitude") ?: 0.0,
                    arguments?.getDouble("longitude") ?: 0.0)
                inimap = "1"
            }
            if (arguments?.getString("latitudeS") != null) {
                riderLatDest = arguments?.getString("latitudeS") ?: ""
                riderLngDest = arguments?.getString("longitudeS") ?: ""
                inimap = "2"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        vwCurrent = inflater.inflate(R.layout.fragment_driver_tracking, container, false)
        return vwCurrent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fg = FuncionesGenerales()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        gpsInfo = GPSTracker(requireContext())
        Common.mLastLocation = gpsInfo.currentLocation
        createLocationRequest()

        Paper.init(requireContext())
        if (Common.db == null) Common.setDbSettings()
        Common.mContext = requireContext()
        Common.mLayoutInflater = this.layoutInflater
        Common.waitingDialog = SpotsDialog.Builder().setContext(requireContext()).build()
        try {
            if (Paper.book().read<Any?>("mtRec") == null) {
                Paper.book().write("mtRec", 0.0)
            } else {
                val mtRec = Paper.book().read<Double>("mtRec")
            }
        } catch (e: Exception) {
            try {
                Paper.book().write("mtRec", 0.0)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
        customerId = ""
        idServicio = ""
        initVariables()
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage?.reference
        arrayListRutes = ArrayList()
        servicio = Services()
        buildGoogleApiClient()
        createLocationRequest()
        showHojaRuta()
        setListeners()
        //checkIfInitTask()
    }

    private fun checkIfInitTask() {

        if (sesionManager?.getLatLng() != null) {
            val ubication = sesionManager?.getLatLng()
            riderLatDest = ubication?.lat
            riderLngDest = ubication?.lng
            if (inimap == "2") {
                getDirection()
            }
        }

    }

    private fun initVariables() {
        try {
            sesionManager?.clearLatLng()
        } finally {
            sesionManager = SesionManager(requireContext())
        }
        btnReDirection = vwCurrent.findViewById(R.id.btnReDirection)
        textDirection = vwCurrent.findViewById(R.id.textDirection)
        imgDropDown = vwCurrent.findViewById(R.id.imgDropDown)
        btnMyLocation = vwCurrent.findViewById(R.id.btnMyLocation)
        txtTimeEst = vwCurrent.findViewById(R.id.txtTimeEst)
        txtDist = vwCurrent.findViewById(R.id.txtDist)
    }

    private fun setListeners() {
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.tarea)
        mediaPlayer?.isLooping = false
        observeServicesPen1()
        fbMainMap.setOnClickListener {
            onAddButtonClicked()
        }
        fbDistanceMap.setOnClickListener {
            Common.ifRute = false
            organizeRuta()
        }
        fbTimeMap.setOnClickListener {
            Common.ifRute = true
            organizeRuta()
        }
        btnMyLocation?.setOnClickListener {
            setMyLocation()
        }
        btnReDirection?.setOnClickListener {
            getDirections()
            setTitleTypeRute()
        }
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            fbTimeMap.startAnimation(fromRight)
            fbDistanceMap.startAnimation(fromRight)
            btnMyLocation?.startAnimation(fromRight)
            fbMainMap.startAnimation(rotateOpen)
        } else {
            fbTimeMap.startAnimation(toRight)
            fbDistanceMap.startAnimation(toRight)
            btnMyLocation?.startAnimation(toRight)
            fbMainMap.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            fbDistanceMap.visibility = View.VISIBLE
            fbTimeMap.visibility = View.VISIBLE
            btnMyLocation?.visibility = View.VISIBLE
        } else {
            fbDistanceMap.visibility = View.INVISIBLE
            fbTimeMap.visibility = View.INVISIBLE
            btnMyLocation?.visibility = View.INVISIBLE
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            fbDistanceMap.isClickable = true
            fbTimeMap.isClickable = true
            btnMyLocation?.isClickable = true
        } else {
            fbDistanceMap.isClickable = false
            fbTimeMap.isClickable = false
            btnMyLocation?.isClickable = false
        }
    }

    private fun setMyLocation() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(gpsInfo.currentLatitude,
            gpsInfo.currentLongitude), 17.0f))
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = FATEST_INTERVAL.toLong()
        mLocationRequest.fastestInterval = FATEST_INTERVAL.toLong()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.smallestDisplacement = DISPLACEMENT.toFloat()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getDirection()
//Set style map
        checkIfInitTask()
        var isSucces = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context,
            R.raw.uber_style_map))
        mMap.uiSettings.isZoomControlsEnabled = true
        setMyLocation()
        setUpLocation()
        if (inimap == "2") {
            getDirection()
        }
    }


    var btnReDirection: FloatingActionButton? = null

    var riderLatDest: String? = null
    var riderLngDest: String? = null

    var customerId: String? = null
    var idServicio: String? = null
    var servicio: Services? = null

    var database = FirebaseDatabase.getInstance()

    private val PLAY_SERVICE_RES_REQUEST = 7001

    private var mGoogleApiClient: GoogleApiClient? = null

    private val UPDATE_INTERVAL = 5000
    private val FATEST_INTERVAL = 3000
    private val DISPLACEMENT = 10

    private val riderMarker: Circle? = null
    private var driverMarker: Marker? = null
    private var pointsMarker = java.util.ArrayList<Marker>()

    private var direction: Polyline? = null
    var arrayListRutes: java.util.ArrayList<Rutes>? = null

    var geoFire: GeoFire? = null

    var btnMyLocation: FloatingActionButton? = null
    var textDirection: TextView? = null
    var txtTimeEst: TextView? = null
    var txtDist: TextView? = null
    var imgDropDown: ImageView? = null

    var waitingDialog: AlertDialog? = null

    var firebaseStorage: FirebaseStorage? = null
    var storageReference: StorageReference? = null


    var cantUpLoad = 0
    var cantUpEmail: Int = 0
    var cantEmail: Int = 0
    var cantImg: Int = 0


    companion object {
        @JvmStatic
        fun newInstance() = DriverTrackingFragment()
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    // Estado 0 = Sin Iniciar recorrido
    // Estado 1 = En recorrido
    // Estado 2 = En tarea
    // Estado 3 = Reporte Demora
    // Estado 4 = Recorrido Reanudado
    // Estado 5 = Recorrido Cancelado
    var updateInfoRed: MutableMap<String, Any>? = null
    private fun recRegistrar(intEstado: Int, zIndex: Int) {

        val fg = FuncionesGenerales()
        val ce = CargarEstados()
        fg.act_param(requireContext(), "ESTADOREC_ACT", intEstado.toString())
        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
        var latact = latitud_ult
        var lonact = longitud_ult
        fg.ins_historial(requireContext(), "0", latact, lonact)
        fg.act_estadotarea(requireContext())
        ce.ActualizarEstado(requireContext())
        recEstado = intEstado
        updateInfoRed = java.util.HashMap<String, Any>()
        val updateInfo2: MutableMap<String, Any> = java.util.HashMap<String, Any>()
        val updateInfo3: MutableMap<String, Any> = java.util.HashMap<String, Any>()
        val horaNow = Date()
        var boolRecKmTiempo = true
        updateInfoRed = Common.mapRegistroUs!!["Recorrido"] as MutableMap<String, Any>?
        val mapLatLng = arrayService.get(zIndex).get("latLng") as MutableMap<String, Any>
        riderLatDest = mapLatLng["latitude"].toString()
        riderLngDest = mapLatLng["longitude"].toString()

        val estadorec = fg.estadorec_act(requireContext())
        if (estadorec.toString() == "0") {
            updateInfoRed!!["RecKmReal"] = Paper.book().read("mtRec")
            updateInfoRed!!["RecTiempoReal"] = "00:00"
        } else if (((Common.mapRegistroUs!!["Recorrido"] as MutableMap<String?, Any>?)!!["Estado"].toString() == "1")) {
            if (!(Common.mapRegistroUs!!["Recorrido"] as MutableMap<String?, Any>?)!!["RecHoraIn"].toString()
                    .isEmpty()
            ) {
                try {
                    val horaIn = Date()
                    val recTiempo = Date()
                    horaIn.hours =
                        Common.formatHora.parse((Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, Any>?)!!.get(
                            "RecHoraIn").toString()).hours
                    horaIn.minutes =
                        Common.formatHora.parse((Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, Any>?)!!.get(
                            "RecHoraIn").toString()).minutes
                    recTiempo.hours = (horaNow.hours) - horaIn.hours
                    recTiempo.minutes = (horaNow.minutes) - horaIn.minutes
                    updateInfoRed!!["RecTiempoReal"] = Common.formatHora.format(recTiempo)
                    updateInfoRed!!["RecKmReal"] = Paper.book().read<Any>("mtRec").toString() + ""
                } catch (e: java.text.ParseException) {
                    e.printStackTrace()
                }
            }
        } else {
            updateInfoRed!!["RecHoraIn2"] = Common.formatHora.format(horaNow)
            val estadorec = fg.estadorec_act(requireContext())
            if (estadorec.toString() != "5" &&
                estadorec.toString() != "2" &&
                estadorec.toString() != "3"
            ) {
                try {
                    val horaIn = Date()
                    val recTiempo = Date()
                    val tiempoReal = Date()
                    horaIn.hours = Common.formatHora.parse(
                        (Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, Any>?)!!.get(
                            "RecHoraIn2"
                        ).toString()
                    ).hours
                    horaIn.minutes = Common.formatHora.parse(
                        (Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, Any>?)!!.get(
                            "RecHoraIn2"
                        ).toString()
                    ).minutes
                    tiempoReal.hours = Common.formatHora.parse(
                        (Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, Any>?)!!.get(
                            "RecTiempoReal"
                        ).toString()
                    ).hours
                    tiempoReal.minutes = Common.formatHora.parse(
                        (Common.mapRegistroUs!!.get("Recorrido") as MutableMap<String?, Any>?)!!.get(
                            "RecTiempoReal"
                        ).toString()
                    ).minutes
                    recTiempo.hours = (horaNow.hours) - horaIn.hours + tiempoReal.hours
                    recTiempo.minutes = (horaNow.minutes) - horaIn.minutes + tiempoReal.minutes
                    updateInfoRed!!["RecTiempoReal"] = Common.formatHora.format(recTiempo)
                    updateInfoRed!!["RecKmReal"] =
                        Paper.book().read<kotlin.Any>("mtRec").toString() + ""
                } catch (e: java.text.ParseException) {
                    e.printStackTrace()
                }
            }
        }
        updateInfoRed!!["Estado"] = intEstado.toString() + ""
        if (intEstado != 0) {
            updateInfoRed!!["IdTarea"] = arrayService.get(zIndex).get("key").toString()
        }
        val mapRed: MutableMap<String, Any?> = java.util.HashMap<String, Any?>()
        try {
            when (intEstado) {
                0 -> {
                    updateInfoRed!!["IdTarea"] = ""
                    updateInfoRed!!["RecHoraFin"] = ""
                    updateInfoRed!!["RecHoraIn"] = ""
                    updateInfoRed!!["RecHoraIn2"] = ""
                    updateInfoRed!!["RecKmEst"] = ""
                    updateInfoRed!!["RecKmReal"] = ""
                    updateInfoRed!!["RecTiempoEst"] = ""
                    updateInfoRed!!["RecTiempoReal"] = ""
                    Common.boolRecKm = false
                    boolRecKmTiempo = false
                    Paper.book().write("mtRec", 0.0)
                    mapRed["Recorrido"] = updateInfoRed
                    Common.dbRegistroUs!!.document(Common.mapRegistroUs!!["key"].toString()).update(
                        mapRed
                    )
                }
                1 -> {
                    Common.boolRecKm = true

                    updateInfoRed!!["ubicacion"] = LatLng(
                        latact.toDouble(), lonact.toDouble())
                    updateInfoRed!!["RecHoraIn"] = Common.formatHora.format(horaNow)
                    Common.recIdTarea = arrayService.get(zIndex).get("key").toString()
                    try {
                        fg.act_estadotareac(requireContext(), idtarea, "En Recorrido")
                    } finally {
                        setTaskInit()
                        setTitleTypeRute()
                        getDirection()
                    }
                }
                2 -> {
                    var mapEje = arrayService.get(zIndex)
                        .get("ejecucion") as kotlin.collections.MutableMap<String?, Any?>
                    mapEje = arrayService.get(zIndex)
                        .get("ejecucion") as kotlin.collections.MutableMap<String?, Any?>
                    mapEje["abierta"] = "1"
                    updateInfo2["ejecucion"] = mapEje
                    Common.dbServices!!.document(arrayService.get(zIndex).get("key").toString())
                        .update(
                            updateInfo2
                        )
                    Common.recIdTarea = arrayService.get(zIndex).get("key").toString()
                    Common.boolRecKm = false
                    updateInfoRed!!["RecHoraIn2"] = Common.formatHora.format(horaNow)
                    boolRecKmTiempo = false
                    mapRed["Recorrido"] = updateInfoRed
                    Common.dbRegistroUs!!.document(Common.mapRegistroUs!!["key"].toString()).update(
                        mapRed
                    )
                    fg.act_estadotareapro(requireContext(), idtarea, "En Proceso")
                }
                3 -> {
                    Common.recIdTarea = arrayService.get(zIndex).get("key").toString()
                    val nota = ("Hora: " + Common.formatHora.format(Date())
                        .toString() + "\n".toString() +
                            "Usuario: " + Common.documentUser!!.get("nombres").toString()
                        .toString() + " " + Common.documentUser!!.get(
                        "apellidos"
                    ).toString().toString() + "\n".toString() +
                            "Nota: Se reporta tiempo muerto")
                    val lts = LoadTaskSqlite()
                    Common.boolRecKm = false
                    boolRecKmTiempo = false
                    lts.historynotes(requireContext(), nota)
                    /*updateInfoRed!!["RecHoraIn2"] = Common.formatHora.format(horaNow)
                    mapRed["Recorrido"] = updateInfoRed
                    Common.dbRegistroUs!!.document(Common.mapRegistroUs!!["key"].toString()).update(
                        mapRed
                    )*/
                }
                4 -> {
                    Common.boolRecKm = true
                    updateInfoRed!!["RecHoraIn2"] = Common.formatHora.format(horaNow)
                    Common.recIdTarea = arrayService.get(zIndex).get("key").toString()
                    try {
                        fg.act_estadotareac(requireContext(), idtarea, "En Recorrido")
                    } finally {
                        setTaskInit()
                        setTitleTypeRute()
                        getDirection()
                    }
                }
                5 -> {
                    try {
                        fg.act_recorridosapend(requireContext())
                    } finally {
                        Common.boolRecKm = false
                        updateInfoRed!!["IdTarea"] = ""
                        updateInfoRed!!["RecHoraIn2"] = Common.formatHora.format(horaNow)
                        updateInfoRed!!["RecKmEst"] = ""
                        updateInfoRed!!["RecTiempoEst"] = ""
                        boolRecKmTiempo = false
                        mapRed["Recorrido"] = updateInfoRed
                        Common.dbRegistroUs!!.document(Common.mapRegistroUs!!["key"].toString())
                            .update(
                                mapRed
                            )
                    }
                }
            }
        } finally {
            if (boolRecKmTiempo) getKmTiempoEst()
            setUpLocation()
            btnReDirection?.callOnClick()
        }

    }

    private fun getKmTiempoEst() {
        val fg = FuncionesGenerales()
        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
        val latdbl = latitud_ult.toDouble()
        val londbl = longitud_ult.toDouble()
        val queue = Volley.newRequestQueue(requireContext())
        // Request a string response from the provided URL.
        val stringRequest = StringRequest(com.android.volley.Request.Method.POST,
            ("https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "departure_time=now&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + latdbl + "," + londbl + "&" +
                    "destination=" + riderLatDest + "," + riderLngDest + "&" +
                    "key=AIzaSyCmtQfrrX6NGPMWoSOqlHi7PsQ0epqGEBo"),
            { response ->
                try {
                    android.util.Log.e("tcgoRute", "1")
                    val jsonService = JSONObject(response)
                    val arrayRoutes = jsonService.getJSONArray("routes")
                    val jsonRoutes = arrayRoutes.getJSONObject(0)
                    val arrayLegs = jsonRoutes.getJSONArray("legs")
                    val jsonLegs = arrayLegs.getJSONObject(0)
                    val arrayDistance = jsonLegs.getJSONObject("distance")
                    val distanceValue = arrayDistance.getString("value")
                    val arrayDuration = jsonLegs.getJSONObject("duration")
                    val durationValue = arrayDuration.getString("value")
                    updateInfoRed!!["RecKmEst"] = distanceValue
                    updateInfoRed!!["RecTiempoEst"] = durationValue
                    val mapRed: MutableMap<String, Any?> =
                        java.util.HashMap<String, Any?>()
                    mapRed["Recorrido"] = updateInfoRed
                    Common.dbRegistroUs!!.document(Common.mapRegistroUs!!["key"].toString())
                        .update(mapRed)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }) { error ->
            error.printStackTrace()
            Toast.makeText(requireContext(), "Problemas de conexión", Toast.LENGTH_LONG).show()
        }

        // Add the request to the RequestQueue.
        queue.add<String>(stringRequest)
    }

    val REQUEST_TAKE_PHOTO = 1
    var currentPhotoPath: String? = null
    var storage = FirebaseStorage.getInstance()
    var storageRef = storage.reference.child("images")

    @Throws(java.io.IOException::class)
    private fun createImageFile(): java.io.File? {
        // Create an image file name
        val timeStamp = Common.formatDateHoraSeg.format(Date())
        val imageFileName = "TCGO_" + timeStamp + "_"
        val storageDir: java.io.File =
            requireActivity().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)!!
        val image = java.io.File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        //        Log.e("tcgoPhoto",currentPhotoPath);
//        Log.e("tcgoPhoto",image.getName());
        return image
    }

    var arrayService = ArrayList<MutableMap<String, Any>>()
    var arrayServiceConsult = ArrayList<MutableMap<String, Any>>()
    var arrayServiceFin = ArrayList<MutableMap<String, Any>>()

    var alertDialog: androidx.appcompat.app.AlertDialog.Builder? = null
    var show: androidx.appcompat.app.AlertDialog? = null
    var horaNow: Date? = null
    var horaRange: Date? = null
    var ruteRange = 0
    var ruteCerca = 0
    private fun organizeRuta() {
        contVip = -1
        contFin = (arrayService.size)
        val horaNow = Date()
        horaNow.year = horaNow.year + 1900
        val horaMin = Date()
        horaMin.year = horaMin.year + 1900
        horaMin.minutes = horaMin.minutes + 30

        /* *
         * Mover las tareas VIP al principio de la ruta si falta media hora o menos para ejecutarce
         * y mover las tareas que ya pasaron la hora de ejecución al final
         */run {
            var i: Int = 0
            while (i < arrayService.size) {
                try {
                    val mapService: MutableMap<String, Any> = arrayService.get(i)
                    val horaTareaIn = Date()
                    horaTareaIn.year = horaTareaIn.year + 1900
                    val horaTareaFin = Date()
                    horaTareaFin.year = horaTareaFin.year + 1900
                    val mapHistory: MutableMap<String, Any>? =
                        (((mapService.get("historial") as MutableMap<String?, Any?>?)!!.get(
                            Common.formatDate.format(
                                Date()
                            )
                        )) as MutableMap<String, Any>?)
                    horaTareaIn.hours = Common.formatHora.parse(
                        mapHistory!!.get("TareaRangoIn").toString()
                    ).hours
                    horaTareaIn.hours = Common.formatHora.parse(
                        mapHistory.get("TareaRangoIn").toString()
                    ).minutes
                    horaTareaFin.hours = Common.formatHora.parse(
                        mapHistory.get("TareaRangoFin").toString()
                    ).hours
                    horaTareaFin.hours = Common.formatHora.parse(
                        mapHistory.get("TareaRangoFin").toString()
                    ).minutes
                    if (horaTareaFin.time < horaNow.time) {
                        if (i == contFin) {
                            i = arrayService.size
                        } else {
                            contFin--
                            temp = mapService
                            arrayService.set(i, arrayService.get(contFin))
                            arrayService.set(contFin, temp)
                            i--
                        }
                    } else if ((mapService.get("prioridad") == "VIP") && horaTareaIn.time <= horaMin.time) {
                        contVip++
                        temp = mapService
                        arrayService.set(i, arrayService.get(contVip))
                        arrayService.set(contVip, temp)
                    }
                } catch (e: java.text.ParseException) {
                    e.printStackTrace()
                }
                i++
            }
        }

        /* *
         * Organizar por distancia y hora las tareas
         */for (i in arrayService.indices) {
            disMin = 0.0
            rango = 0.0
            for1 = true
            for2 = true
            for3 = true
            for (j in i..contVip) {
                if (Common.ifRute) {
                    verificarArray(i, j, 1)
                } else {
                    verificarArray2(i, j)
                }
            }
            var j: Int = i
            while (j > contVip && j < contFin) {
                if (Common.ifRute) {
                    verificarArray(i, j, 2)
                } else {
                    verificarArray2(i, j)
                }
                j++
            }
            while (j >= contFin && j < (arrayService.size - 1)) {
                if (Common.ifRute) {
                    verificarArray(i, j, 3)
                } else {
                    verificarArray2(i, j)
                }
                j++
            }
        }
        ruteRange = 0
        ruteCerca = 0
        for (i in 0 until contFin) {
            if ((arrayService.get(i).get("estado").toString() == "En Proceso")) {
                positionSelected = i
            }

            try {
                val mapService: MutableMap<String, Any> = arrayService.get(i)
                val mapHistory =
                    (((mapService["historial"] as MutableMap<String?, Any?>?)!![Common.formatDate.format(
                        Date()
                    )]) as MutableMap<String, Any>?)
                val dateNow = Date()
                dateNow.year = dateNow.year + 1900
                dateNow.hours = dateNow.hours + 2
                val horaTareaIn = Date()
                horaTareaIn.year = horaTareaIn.year + 1900
                horaTareaIn.hours = Common.formatHora.parse(
                    mapHistory!!.get("TareaRangoIn").toString()
                ).hours
                horaTareaIn.minutes = Common.formatHora.parse(
                    mapHistory.get("TareaRangoIn").toString()
                ).minutes
                if (horaTareaIn.time <= dateNow.time) {
                    ruteRange = i
                }
            } catch (e: java.text.ParseException) {
                e.printStackTrace()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        for (i in 0..ruteRange) {
            distanceRuta = 0.0
            if (arrayService.size != 0) {
                try {
                    val mapLatLng = arrayService.get(i).get("latLng") as MutableMap<String, Any>
                    getDistancia(
                        i,
                        mapLatLng["latitude"].toString().toDouble(),
                        mapLatLng["longitude"].toString().toDouble()
                    )
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else {
                waitingDialog!!.dismiss()
                Toast.makeText(requireContext(), "Sin Tareas", Toast.LENGTH_SHORT).show()
            }
        }
        try {
            show!!.dismiss()
            waitingDialog!!.dismiss()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun setTitleTypeRute() {
        if (Common.ifRute) {
            tvTitleTypeRute.text = getString(R.string.type_rute_time)
        } else {
            tvTitleTypeRute.text = getString(R.string.type_rute_distance)
        }
    }

    fun verificarArray2(i: Int, j: Int) {
        val latdbl = Common.mLastLocation?.latitude
        val londbl = Common.mLastLocation?.longitude
        disMin = if (i == 0) {
            Tools.calcCrow(latdbl ?: 0.0, londbl ?: 0.0,
                Tools.createMutableMap(j, arrayService)["latitude"].toString().toDouble(),
                Tools.createMutableMap(j, arrayService)["longitude"].toString()
                    .toDouble()) * 1000 / 210
        } else {
            Tools.calcCrow(
                Tools.createMutableMap(i - 1, arrayService)["latitude"].toString().toDouble(),
                Tools.createMutableMap(i - 1, arrayService)["longitude"].toString().toDouble(),
                Tools.createMutableMap(j, arrayService)["latitude"].toString().toDouble(),
                Tools.createMutableMap(j, arrayService)["longitude"].toString()
                    .toDouble()) * 1000 / 210
        }
        if (for1) {
            rango = disMin
            for1 = false
        }
        if (disMin < rango) {
            rango = disMin
            temp = arrayService[j]
            arrayService[j] = arrayService[i]
            arrayService[i] = temp
        }
    }

    var contVip = -1
    var contFin = 0
    var temp: MutableMap<String, Any> = HashMap<String, Any>()
    var for1 = true
    var for2: Boolean = true
    var for3: Boolean = true
    var disMin = 0.0
    var rango: Double = 0.0
    var rango3: Double = 0.0

    fun verificarArray(i: Int, j: Int, mFor: Int) {
        val fg = FuncionesGenerales()
        val mapHistory = (((arrayService.get(j)
            .get("historial") as MutableMap<String?, Any?>)[Common.formatDate.format(
            Date()
        )]) as MutableMap<String, Any>?)
        val mapLatLng = arrayService.get(j).get("latLng") as MutableMap<String, Any>
        try {
            val horaTareaFin = Date()
            horaTareaFin.year = horaTareaFin.year + 1900
            horaTareaFin.hours = Common.formatHora.parse(
                mapHistory!!.get("TareaRangoFin").toString()
            ).hours
            horaTareaFin.minutes = Common.formatHora.parse(
                mapHistory.get("TareaRangoFin").toString()
            ).minutes

            if (i == 0) {
                val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
                val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
                val latdbl = latitud_ult.toDouble()
                val londbl = longitud_ult.toDouble()
                disMin = (Tools.calcCrow(
                    latdbl as Double,
                    londbl as Double,
                    mapLatLng["latitude"].toString().toDouble(),
                    mapLatLng["longitude"].toString().toDouble()
                ) * 1000) / 210
            } else {
                val mapLatLng2 = arrayService.get(i - 1).get("latLng") as MutableMap<String, Any>
                disMin = (Tools.calcCrow(
                    mapLatLng2["latitude"].toString().toDouble(),
                    mapLatLng2["longitude"].toString().toDouble(),
                    mapLatLng["latitude"].toString().toDouble(),
                    mapLatLng["longitude"].toString().toDouble()
                ) * 1000) / 210
            }
            val horaLlegada = Date()
            horaLlegada.year = horaLlegada.year + 1900
            horaLlegada.minutes = (horaLlegada.minutes + disMin).toInt()
            val rango2 = horaTareaFin.time - horaLlegada.time.toDouble()
            if (rango2 > 0) {
                when (mFor) {
                    1 -> if (for1) {
                        rango = rango2
                        for1 = false
                    }
                    2 -> if (for2) {
                        rango = rango2
                        for2 = false
                    }
                }
                if (rango2 < rango) {
                    rango = rango2
                    temp = arrayService.get(j)
                    arrayService.set(j, arrayService.get(i))
                    arrayService.set(i, temp)
                }
            } else {
                if (rango3 == 0.0) {
                    rango3 = rango2
                }
                if (rango2 < rango3) {
                    rango3 = rango2
                    temp = arrayService.get(j)
                    when (mFor) {
                        1 -> if (j != contVip) {
                            arrayService.set(j, arrayService.get(contVip))
                            arrayService.set(contVip, temp)
                            verificarArray(i, j, 1)
                        }
                        2 -> if (j < contFin - 1) {
                            arrayService.set(j, arrayService.get(contFin - 1))
                            arrayService.set(contFin - 1, temp)
                            verificarArray(i, j, 2)
                        }
                        3 -> if (j < (arrayService.size - 1)) {
                            arrayService.set(j, arrayService.get(arrayService.size - 1))
                            arrayService.set(arrayService.size - 1, temp)
                            verificarArray(i, j, 3)
                        }
                    }
                }
            }
        } catch (e: java.text.ParseException) {
            e.printStackTrace()
        }
    }

    var boolRute = true

    var positionSelected = -1
    var distanceRuta = 0.0
    var idRuta = 0
    private fun getDistancia(index: Int, lat: Double, lon: Double) {
        val fg = FuncionesGenerales()
        try {
            val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
            val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
            val latdbl = latitud_ult.toDouble()
            val londbl = longitud_ult.toDouble()
            val metros = Tools.calcCrow(
                latdbl as Double,
                londbl as Double,
                lat, lon
            ) * 1000
            if (distanceRuta == 0.0) {
                distanceRuta = metros
                idRuta = index
            } else if (distanceRuta > metros) {
                distanceRuta = metros
                idRuta = index
            }
            ruteCerca++
            if (ruteCerca == (ruteRange + 1)) {
                arrayService.get(idRuta).put("Geocerca", "1")
                setUpLocation()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun showHojaRuta() {
        if (arrayService.size == 0) {
            try {
                arrayService = Tools.chargeArrayService()
            } finally {
                val loadtasksql = LoadTaskSqlite()
                loadtasksql.cargarservices(requireContext(), arrayService)
            }
        }
        if (arrayServiceFin.size == 0) {
            arrayServiceFin = Tools.chargeArrayServiceFinish()
        }
        if (arrayService.size != 0) {
            organizeRuta()
        } else {
            Toast.makeText(requireContext(), getString(R.string.no_tasks), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setUpLocation() {
        if (checkPlayServices()) {
            displayLocation()
        }
    }

    private fun checkPlayServices(): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(requireContext())
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) GooglePlayServicesUtil.getErrorDialog(
                resultCode,
                requireActivity(),
                PLAY_SERVICE_RES_REQUEST
            ).show() else {
                Toast.makeText(requireContext(),
                    getString(R.string.this_device_not_supported),
                    Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
            return false
        }
        return true
    }

    private fun buildGoogleApiClient() {
        /* mGoogleApiClient = GoogleApiClient.Builder(requireContext())
                 .addConnectionCallbacks(this)
                 .addOnConnectionFailedListener(this)
                 .addApi(LocationServices.API)
                 .build()
         mGoogleApiClient!!.connect()*/
    }


    private fun displayLocation() {
        val fg = FuncionesGenerales()
        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
        val latdbl = latitud_ult.toDouble()
        val londbl = longitud_ult.toDouble()
        try {
            mMap.clear()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Common.mLastLocation = gpsInfo.currentLocation
        if (Common.mLastLocation != null || (latdbl != 0.0 && londbl != 0.0)) {
            var latitude: Double? = 0.0
            var longitude: Double? = 0.0
            latitude = Common.mLastLocation?.latitude
            longitude = Common.mLastLocation?.longitude
            fg.act_param(requireContext(), "ULT_LAT", latitude.toString())
            fg.act_param(requireContext(), "ULT_LON", longitude.toString())

            try {
                driverMarker?.remove()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            pointsMarker = java.util.ArrayList<Marker>()
            driverMarker = mMap.addMarker(latitude?.let {
                longitude?.let { it1 -> LatLng(it, it1) }
            }?.let {
                MarkerOptions()
                    .position(it)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                    .title("Tu")
            })
            for (i in arrayService.indices) {
                val mapHistory = (((arrayService.get(i)
                    .get("historial") as MutableMap<String?, Any?>)[Common.formatDate.format(Date())]) as MutableMap<String, Any>?)
                val codetareax = arrayService.get(i).get("codetarea").toString()
                fg.act_param(requireContext(), "TAREA_ACT", codetareax)
                val maxestrec = fg.getQ1(requireContext(),
                    "select ifnull(max(idhistorial),0) as mid from '102_HISTORIAL' WHERE idtarea='$codetareax' and useruid='" + fg.useruid(
                        requireContext()) + "' and fecha='" + fg.fechaActual(1) + "'")
                val estadorec = fg.getQ1(requireContext(),
                    "select ifnull(estadorec,'0') from '102_HISTORIAL' WHERE  idtarea='$codetareax' and idhistorial='" + maxestrec + "' and fecha='" + fg.fechaActual(
                        1) + "'")
                val taer = fg.tarrecorridoact(requireContext())
                val taep = fg.tarprocesoact(requireContext())
                val mapLatLng = arrayService.get(i).get("latLng") as MutableMap<String, Any>
                try {
                    arrayService.get(i).remove("marker")
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                if (taer == "1") {
                    arrayService.get(i).put(
                        "marker", (mMap.addMarker(
                            MarkerOptions().position(
                                LatLng(mapLatLng["latitude"].toString().toDouble(),
                                    mapLatLng["longitude"].toString().toDouble())
                            )
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                                .title(mapHistory!!["TareaRangoIn"].toString() + " - " + mapHistory["TareaRangoFin"].toString())
                                .zIndex(i.toFloat())
                        ))
                    )
                    if (boolRute) {
                        riderLatDest = mapLatLng["latitude"].toString()
                        riderLngDest = mapLatLng["longitude"].toString()
                    }
                } else if (taep == "1" && positionSelected == i) {
                    arrayService.get(i).put(
                        "marker", (mMap.addMarker(
                            MarkerOptions().position(
                                LatLng(mapLatLng["latitude"].toString().toDouble(),
                                    mapLatLng["longitude"].toString().toDouble())
                            )
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                                .title(mapHistory!!["TareaRangoIn"].toString() + " - " + mapHistory["TareaRangoFin"].toString())
                                .zIndex(i.toFloat())
                        ))
                    )
                } else {
                    arrayService.get(i).put(
                        "marker", (mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(mapLatLng["latitude"].toString().toDouble(),
                                    mapLatLng["longitude"].toString().toDouble()))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                .title(mapHistory!!["TareaRangoIn"].toString() + " - " + mapHistory["TareaRangoFin"].toString())
                                .zIndex(i.toFloat())
                        ))
                    )
                }
            }
            boolRute = false
            if (camara) {
                if (ubicateTasks != null) {
                    val cameraPosition =
                        CameraPosition.Builder().target(ubicateTasks).zoom(15f).build()
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                } else {
                    if (arrayService.size > 0) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(getLocationBounds(),
                            200))
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude!!,
                            longitude!!), 17.0f))
                    }
                }
                camara = false
            }
        } else {
            android.util.Log.d("MyError", "No puedo obtener tu posicion")
        }
        mMap.setOnMarkerClickListener {
            if (it.title != "Tu") {
                showTarea(it.zIndex.toInt())
            } else {
                false
            }
        }
    }

    var camara = true

    private fun observeServicesPen1() {
        viewModelActivity.fetchArrayServicesPen1.observe(viewLifecycleOwner, { result ->
            when (result) {
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    val sm = SesionManager(requireContext())
                    arrayServiceConsult = Tools.chargeArrayService()
                    val loadtasksql = LoadTaskSqlite()
                    loadtasksql.cargarservices(requireContext(), arrayServiceConsult)
                    if (sm.getIntTasks() == 0) {
                        sm.setIntTasks(arrayServiceConsult.size)
                    }
                    if (sm.getIntTasks() < arrayServiceConsult.size) {
                        sm.setIntTasks(arrayServiceConsult.size)
                        mediaPlayer?.start()
                        arrayService = Tools.chargeArrayService()
                        displayLocation()
                        Toast.makeText(requireContext(),
                            getString(R.string.do_you_have_new_tasks),
                            Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Failure -> {
                    Toast.makeText(requireContext(),
                        "Ocurrio un error ${result.exception.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getLocationBounds(): LatLngBounds {
        val builder = LatLngBounds.Builder()
        for (services in arrayService) {
            val map = services["latLng"] as MutableMap<String, Any>
            val latLng = LatLng(map["latitude"].toString().toDouble(),
                map["longitude"].toString().toDouble())
            builder.include(latLng)
        }
        return builder.build()
    }

    private fun setTaskInit() {
        val ubication = Ubication(riderLatDest, riderLngDest)
        sesionManager?.setLatLng(ubication)
    }

    private fun showTarea(zIndex: Int): Boolean {
        val fg = FuncionesGenerales()
        positionSelected = zIndex
        recEstado = Tools.getRecEstado()
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val inflater: LayoutInflater = requireActivity().layoutInflater
        val validate_info_task = inflater.inflate(R.layout.layout_info_task, null)
        val txtEstado = validate_info_task.findViewById<TextView>(R.id.txtEstado)
        val txtId = validate_info_task.findViewById<TextView>(R.id.txtId)
        val txtDateRange = validate_info_task.findViewById<TextView>(R.id.txtDateRange)
        val txtTimeEstimado = validate_info_task.findViewById<TextView>(R.id.txtTimeEstimado)
        val txtMetrosTarea = validate_info_task.findViewById<TextView>(R.id.txtMetrosTarea)
        val txtTypeService = validate_info_task.findViewById<TextView>(R.id.txtTypeServiceInfoTask)
        val txtPiezas = validate_info_task.findViewById<TextView>(R.id.txtPiezas)
        val txtNomClient = validate_info_task.findViewById<TextView>(R.id.txtNomClient)
        val txtDirection = validate_info_task.findViewById<TextView>(R.id.txtDirection)
        val txtTel = validate_info_task.findViewById<TextView>(R.id.txtTel)
        val txtNotas = validate_info_task.findViewById<TextView>(R.id.txtNotas)
        val txtStandBy = validate_info_task.findViewById<TextView>(R.id.txtStandBy)
        val imgPrioridad = validate_info_task.findViewById<ImageView>(R.id.imgPrioridad)
        val imgCerrar = validate_info_task.findViewById<ImageView>(R.id.imgCerrar)
        val btnInFinTarea = validate_info_task.findViewById<Button>(R.id.btnInFinTarea)
        val btnInRecTarea = validate_info_task.findViewById<Button>(R.id.btnInRecTarea)
        val btnStandBy = validate_info_task.findViewById<TextView>(R.id.btnStandBy)
        val btnInFinTiempoMuerto =
            validate_info_task.findViewById<Button>(R.id.btnInFinTiempoMuerto)
        val imgEditDir = validate_info_task.findViewById<ImageView>(R.id.ImgEditDir)
        val showListTasks = validate_info_task.findViewById<Button>(R.id.btnShowTaskMap)
        idtarea = arrayService[zIndex]["codetarea"].toString()
        val fa = fg.fechaActual(1)
        val crerec = fg.getQ1(requireContext(),
            "Select ifnull(count(*),'0') as crep from '200_TAREAS' where estado='En Recorrido' and (activa='1' or abierta='1')")
        if (crerec.toInt() > 1) {
            val maxidhisttare = fg.getQ1(requireContext(),
                "select max(idhistorial) from '102_HISTORIAL' where fecha='$fa'  and idtarea='$idtarea'")
            val cterest1 = fg.getQ1(requireContext(),
                "select estadorec from '102_historial' where fecha='$fa' and idtarea='$idtarea' and idhistorial='$maxidhisttare'")
            if (cterest1 == "1") {
                fg.ejecDB(requireContext(),
                    "update '200_TAREAS' set estado='Pendiente' where fecha='$fa' and idtarea='$idtarea'")
                val ce = CargarEstados()
                ce.ActualizarServicesp(requireContext(), idtarea)
            }
        }
        val tarenproc = fg.getQ1(requireContext(),
            "Select ifnull(count(*),'0') as crep from '200_TAREAS' where estado='En Proceso' and (activa='1' or abierta='1')")
        if (tarenproc != "0") {
            fg.act_param(requireContext(), "ESTADOREC_ACT", "2")
        }
        val met = fg.parametro(requireContext(), "RANGOMTS").toInt()

        if (recEstado == 1 || recEstado == 4) {
            imgEditDir.visibility = View.VISIBLE
        }
        fg.act_param(requireContext(), "TAREA_ACT", idtarea)

        imgEditDir.setOnClickListener {
            showDialogEditDirection()
        }
        showListTasks.setOnClickListener {
            val map = arrayService[zIndex]["latLng"] as MutableMap<String, Any>
            ubicateTasks = LatLng(map["latitude"].toString().toDouble(),
                map["longitude"].toString().toDouble())
            displayLocation()
            showTarea?.dismiss()
        }
        showListTasks.visibility = View.GONE

        estadotask = fg.getQ1(requireContext(),
            "Select estado from '200_TAREAS' where idtarea='" + idtarea + "'")
            .toString()
        fg.act_param(requireContext(), "ESTADO_ACT", estadotask.toString())

        val ntarea =
            fg.getQ1(requireContext(),
                "SELECT tarea from '200_TAREAS' where idtarea='" + idtarea + "'").toString()
        val idformulario =
            fg.getQ1(requireContext(),
                "SELECT idformulario from '108_FORMULARIOS' where idtarea='" + idtarea + "'")
                .toString()
        val nformulario =
            fg.getQ1(requireContext(),
                "SELECT formulario from '108_FORMULARIOS' where idtarea='" + idtarea + "'")
                .toString()
        val tipotarea = arrayService[zIndex]["tipoTarea"].toString()
        txtId.text = idtarea.toString().substring(11)

        fg.act_param(requireContext(), "NTAREA_ACT", ntarea)
        fg.act_param(requireContext(), "FORMULARIO_ACT", idformulario)
        fg.act_param(requireContext(), "NFORMULARIO_ACT", nformulario)
        fg.act_param(requireContext(), "TIPO_GRP", tipotarea)
        val estadotarea = fg.getQ1(requireContext(),
            "SELECT estado from '200_TAREAS' WHERE idtarea='" + idtarea + "'")
        if (estadotarea.equals("Finalizado")) {
            fg.ejecDB(requireContext(),
                "DELETE FROM '301_RESPUESTAS_TEMP' WHERE idtarea='" + idtarea + "';")
            fg.ejecDB(requireContext(),
                "DELETE FROM '302_FOTOS_RESP_TEMP' WHERE idtarea='" + idtarea + "';")
            fg.ejecDB(requireContext(),
                "INSERT OR IGNORE INTO '301_RESPUESTAS_TEMP'(idtarea,idformulario,idform,grpid,sgrpid,idpreg,preg,opn,opt,rta1,rta2,obs,gpslat,gpslon,useruid) SELECT idtarea,idformulario,idform,grpid,sgrpid,idpreg,preg,opn,opt,rta1,rta2,obs,gpslat,gpslon,useruid from '301_RESPUESTAS' WHERE idtarea='" + idtarea + "' order by 1,2,3,4,5,6 ASC;")
            fg.ejecDB(requireContext(),
                "INSERT OR IGNORE INTO '302_FOTOS_RESP_TEMP'(idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,idfoto,nfoto,gpslat,gpslon,useruid) SELECT idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,idfoto,nfoto,gpslat,gpslon,useruid from '302_FOTOS_RESP' WHERE idtarea='" + idtarea + "' order by 1,2,3,4,5,6 ASC;")
        } else {
            fg.ejecDB(requireContext(),
                "INSERT OR IGNORE INTO '301_RESPUESTAS_TEMP'(idtarea,idformulario,idform,grpid,sgrpid,idpreg,preg,opn,opt) SELECT '204_PREGGEN'.idtarea , '204_PREGGEN'.idformulario , '204_PREGGEN'.idform ,'204_PREGGEN'.grpid,'204_PREGGEN'.sgrpid,'204_PREGGEN'.idpreg,'204_PREGGEN'.preg,'0' as opn,'0' as opt from '204_PREGGEN' LEFT JOIN '301_RESPUESTAS_TEMP' ON '204_PREGGEN'.idtarea = '301_RESPUESTAS_TEMP'.idtarea and  '204_PREGGEN'.idformulario = '301_RESPUESTAS_TEMP'.idformulario and  '204_PREGGEN'.idform = '301_RESPUESTAS_TEMP'.idform and  '204_PREGGEN'.grpid = '301_RESPUESTAS_TEMP'.grpid and  '204_PREGGEN'.sgrpid = '301_RESPUESTAS_TEMP'.sgrpid and  '204_PREGGEN'.idpreg = '301_RESPUESTAS_TEMP'.idpreg  WHERE '204_PREGGEN'.idtarea='" + idtarea + "' and '204_PREGGEN'.idform<>'9991' and '204_PREGGEN'.idform<>'9992' and ('301_RESPUESTAS_TEMP'.idtarea  is null or  '301_RESPUESTAS_TEMP'.idformulario  is null or '301_RESPUESTAS_TEMP'.idform  is null or '301_RESPUESTAS_TEMP'.grpid is null or '301_RESPUESTAS_TEMP'.sgrpid is null or '301_RESPUESTAS_TEMP'.idpreg is null) order by 1,2,3,4,5,6 ASC;")
        }
        txtId.setOnClickListener {
            Toast.makeText(requireContext(), "Código Copiado", Toast.LENGTH_SHORT).show()
            val clipboard =
                requireActivity().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText("label", txtId.text)
            clipboard.setPrimaryClip(clip)
        }
        val mapHistory = (((arrayService.get(zIndex)
            .get("historial") as MutableMap<String?, Any?>)[Common.formatDate.format(Date())]) as MutableMap<String, Any>?)
        val mapInfoCliente = arrayService.get(zIndex).get("infoCliente") as MutableMap<String, Any>
        val mapLatLng = arrayService.get(zIndex).get("latLng") as MutableMap<String, Any>
        txtEstado.text = arrayService.get(zIndex).get("estado").toString()
        txtDateRange.text =
            mapHistory!!.get("TareaRangoIn").toString() + " - " + mapHistory.get("TareaRangoFin")
        txtTimeEstimado.text = mapHistory.get("TareaTiempoEst").toString()
        txtTypeService.text = fg.typeservice(requireContext())
        txtPiezas.text = arrayService.get(zIndex).get("piezaCant").toString()
        txtNomClient.text = mapInfoCliente.get("nomClient").toString()
        txtDirection.text =
            " ${getString(R.string.direction_spanish)}: \n${mapInfoCliente["direction"].toString()}"
        txtTel.text =
            mapInfoCliente.get("tel1").toString() + " - " + mapInfoCliente.get("tel2").toString()
        txtNotas.text = arrayService.get(zIndex).get("nota").toString()

        if ((arrayService.get(zIndex).get("prioridad")
                .toString() == "VIP")
        ) imgPrioridad.visibility = View.VISIBLE
        if ((arrayService.get(zIndex).get("standBy").toString() == "1") && (arrayService.get(zIndex)
                .get("estado").toString() == "Pendiente")
        ) {
            txtStandBy.visibility = View.VISIBLE
        }
        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
        val latdbl = latitud_ult.toDouble()
        val londbl = longitud_ult.toDouble()
        val metros = Tools.calcCrow(
            latdbl as Double,
            londbl as Double,
            mapLatLng["latitude"].toString().toDouble(),
            mapLatLng["longitude"].toString().toDouble()) * 1000
        val formatDecimal = java.text.DecimalFormat("#.00")
        val distancia = metros / 1000
        if (distancia <= 1) {
            txtMetrosTarea.text = "${formatDecimal.format(distancia * 1000)}  Mts"
        } else {
            txtMetrosTarea.text = formatDecimal.format(distancia) + " Km"
        }
        btnInFinTarea.visibility = View.GONE
        btnInRecTarea.visibility = View.GONE
        btnInFinTiempoMuerto.visibility = View.GONE
        btnStandBy.visibility = View.GONE

        val tep = fg.haytarproceso(requireContext())
        val taep = fg.tarprocesoact(requireContext())
        val ter = fg.haytarrecorrido(requireContext())
        val taer = fg.tarrecorridoact(requireContext())
        val taes = fg.tarstandbyact(requireContext())


        val estadorec = fg.estadorec_act(requireContext())

        if (tep == "0" || taep == "1") {
            when (estadorec.toString()) {
                "0", "5" -> if (metros < met) {
                    if (taes == "1") {
                        btnInFinTarea.text = "FINALIZAR TAREA"
                    }
                    btnInFinTarea.visibility = View.VISIBLE
                } else {
                    if (taer == "1") {
                        btnInRecTarea.text = "CANCELAR RECORRIDO"
                    }
                    btnInRecTarea.visibility = View.VISIBLE
                }
                "1", "4" -> if (taer == "1") {
                    if (metros < met) {
                        btnInFinTarea.visibility = View.VISIBLE
                        btnInFinTiempoMuerto.visibility = View.VISIBLE
                    } else {
                        btnInRecTarea.text = "CANCELAR RECORRIDO"
                        btnInRecTarea.visibility = View.VISIBLE
                    }
                } else {
                    if (metros < met) {
                        btnInFinTarea.visibility = View.VISIBLE
                    } else {
                        btnInRecTarea.text = "REDIRIGIR A ESTA TAREA"
                        btnInRecTarea.visibility = View.VISIBLE
                    }
                }
                "2" -> if (taep == "1") {
                    if (metros < met) {
                        btnInFinTarea.text = "FINALIZAR TAREA"
                        btnInFinTarea.visibility = View.VISIBLE
                    }
                    btnStandBy.visibility = View.VISIBLE
                }
                "3" -> if (Common.recIdTarea.equals(arrayService.get(zIndex).get("key")
                        .toString())
                ) {
                    if (metros < met) {
                        btnInFinTarea.visibility = View.VISIBLE
                    }
                } else {
                    if (metros < met) {
                        btnInFinTarea.visibility = View.VISIBLE
                    } else {
                        btnInRecTarea.text = "REDIRIGIR A ESTA TAREA"
                        btnInRecTarea.visibility = View.VISIBLE
                    }
                }
            }
        }
//        btnInFinTarea
//        btnInRecTarea
//        btnInFinTiempoMuerto
        alertDialog.setView(validate_info_task)
        try {
            showTarea = alertDialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        btnInRecTarea.setOnClickListener {
            try {
                showTarea!!.dismiss()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            if ((Common.ifAlmuerzo != 1) && (Common.ifBreack != 1) && (Common.ifPermiso != 1)) {
                updateInfoRed = java.util.HashMap<String, Any>()
                when (btnInRecTarea.text.toString()) {
                    "INICIAR RECORRIDO" -> {
                        try {
                            fg.act_estadotareac(requireContext(), idtarea, "En Recorrido")
                        } finally {
                            try {
                                if (estadorec.toString() == "5") {
                                    recRegistrar(4, zIndex)
                                } else {
                                    recRegistrar(1, zIndex)
                                }
                            } finally {
                                if (metros < met) {
                                    btnInFinTarea.visibility = View.VISIBLE
                                    btnInRecTarea.visibility = View.GONE
                                    btnInFinTiempoMuerto.visibility = View.VISIBLE
                                } else {
                                    btnInFinTarea.visibility = View.GONE
                                    btnInRecTarea.visibility = View.VISIBLE
                                    btnInRecTarea.text = "CANCELAR RECORRIDO"
                                    btnInFinTiempoMuerto.visibility = View.GONE
                                }
                            }
                        }
                    }
                    "REDIRIGIR A ESTA TAREA" -> {
                        try {
                            fg.act_estadotareac(requireContext(), idtarea, "En Recorrido")
                        } finally {
                            try {
                                if (estadorec.toString() == "1") {
                                    recRegistrar(1, zIndex)
                                } else {
                                    recRegistrar(4, zIndex)
                                }
                            } finally {
                                if (metros < met) {
                                    btnInFinTarea.visibility = View.VISIBLE
                                    btnInRecTarea.visibility = View.GONE
                                    btnInFinTiempoMuerto.visibility = View.VISIBLE
                                } else {
                                    btnInFinTarea.visibility = View.GONE
                                    btnInRecTarea.visibility = View.VISIBLE
                                    btnInRecTarea.text = "CANCELAR RECORRIDO"
                                    btnInFinTiempoMuerto.visibility = View.GONE
                                }
                            }
                        }
                    }
                    "CANCELAR RECORRIDO" -> {
                        try {
                            fg.act_recorridosapend(requireContext())
                        } finally {
                            sesionManager?.clearLatLng()
                            btnInRecTarea.text = "INICIAR RECORRIDO"
                            recRegistrar(5, zIndex)
                            btnInFinTarea.visibility = View.GONE
                            btnInRecTarea.visibility = View.VISIBLE
                            btnInFinTiempoMuerto.visibility = View.GONE
                        }
                    }
                }

            } else {
                if (Common.ifAlmuerzo == 1) {
                    Toast.makeText(requireContext(),
                        "Verifica tu horario de Almuerzo",
                        Toast.LENGTH_SHORT).show()
                }
                if (Common.ifBreack == 1) {
                    Toast.makeText(requireContext(),
                        "Verifica tu horario de Breack",
                        Toast.LENGTH_SHORT).show()
                }
                if (Common.ifPermiso == 1) {
                    Toast.makeText(requireContext(),
                        "Verifica tu horario de Permiso",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
        btnInFinTiempoMuerto.setOnClickListener {
            if ((Common.ifAlmuerzo != 1) && (Common.ifBreack != 1) && (Common.ifPermiso != 1)) {
                recRegistrar(3, zIndex)
                btnInFinTarea.visibility = View.VISIBLE
                btnInRecTarea.visibility = View.GONE
                btnInFinTiempoMuerto.visibility = View.GONE
            } else {
                if (Common.ifAlmuerzo == 1) {
                    Toast.makeText(
                        requireContext(),
                        "Verifica tu horario de Almuerzo",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                }
                if (Common.ifBreack == 1) {
                    Toast.makeText(
                        requireContext(),
                        "Verifica tu horario de Breack",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                }
                if (Common.ifPermiso == 1) {
                    Toast.makeText(
                        requireContext(),
                        "Verifica tu horario de Permiso",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        btnInFinTarea.setOnClickListener {
            if ((Common.ifAlmuerzo != 1) && (Common.ifBreack != 1) && (Common.ifPermiso != 1)) {
                val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
                val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
                var latact = latitud_ult
                var lonact = longitud_ult
                val latdbl = latitud_ult.toDouble()
                val londbl = longitud_ult.toDouble()
                val metros = Tools.calcCrow(
                    latdbl as Double,
                    londbl as Double,
                    mapLatLng["latitude"].toString().toDouble(),
                    mapLatLng["longitude"].toString().toDouble()
                ) * 1000
                if (metros <= met) {
                    val loadtasksql = LoadTaskSqlite()
                    loadtasksql.resetParametros(requireContext())
                    when (btnInFinTarea.text.toString()) {
                        "INICIAR TAREA" -> {
                            btnInFinTarea.text = "FINALIZAR TAREA"
                            fg.act_param(requireContext(), "ESTADO_ACT", "En Proceso")
                            fg.act_standby(requireContext(), "0")
                            recRegistrar(2, zIndex)
                            btnInFinTarea.visibility = View.VISIBLE
                            btnInRecTarea.visibility = View.GONE
                            btnInFinTiempoMuerto.visibility = View.GONE
                            btnInFinTarea.callOnClick()
                        }
                        "FINALIZAR TAREA" -> {
                            showTarea!!.dismiss()
                            openformaFragment()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(),
                        "Debes estar a 200 metros de la tarea",
                        Toast.LENGTH_SHORT).show()
                }
            } else {
                if (Common.ifAlmuerzo == 1) {
                    Toast.makeText(requireContext(),
                        "Verifica tu horario de Almuerzo",
                        Toast.LENGTH_SHORT).show()
                }
                if (Common.ifBreack == 1) {
                    Toast.makeText(requireContext(),
                        "Verifica tu horario de Breack",
                        Toast.LENGTH_SHORT).show()
                }
                if (Common.ifPermiso == 1) {
                    Toast.makeText(requireContext(),
                        "Verifica tu horario de Permiso",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
        btnStandBy.setOnClickListener {
            showTarea?.dismiss()
            val inflater =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            popupstandby(inflater)
        }
        imgCerrar.setOnClickListener {
            try {
                showTarea!!.dismiss()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    private fun showDialogEditDirection() {
        val dialogFragment = EditDirectionDialogFragment()
        val bundle = Bundle()
        bundle.putInt("zIndex", positionSelected)
        bundle.putString("startDir", startDir)
        dialogFragment.arguments = bundle
        dialogFragment.show(childFragmentManager, "DialogWebView")
    }

    var showTarea: androidx.appcompat.app.AlertDialog? = null
    var builder: androidx.appcompat.app.AlertDialog.Builder? = null

    private fun popupstandby(inflater: LayoutInflater) {
        val fg = FuncionesGenerales()
        val lts = LoadTaskSqlite()
        val popUp = inflater.inflate(R.layout.stanby_layout, null)
        val titulo = popUp.findViewById<View>(R.id.tvTitulo) as TextView
        val motivo = popUp.findViewById<View>(R.id.etObservation) as EditText
        titulo.setText("Ingrese el motivo para detener la ejecucion de la tarea")
        val cancel = popUp.findViewById<View>(R.id.btnCancelarSB) as TextView
        val ok = popUp.findViewById<View>(R.id.btnIngresarST) as TextView
        val popupWindow = PopupWindow(popUp, LinearLayout.LayoutParams.MATCH_PARENT, 500, true)
        val bk = Backups()
        bk.backupdDatabase(requireContext())
        cancel.setOnClickListener { popupWindow.dismiss() }
        ok.setOnClickListener {
            val latitud_ult = fg?.parametro(requireContext(), "ULT_LAT")
            val longitud_ult = fg?.parametro(requireContext(), "ULT_LON")
            var latact = latitud_ult
            var lonact = longitud_ult
            if (motivo.text.toString().length > 3) {
                try {
                    fg.act_estadotareaandsync(requireContext(), idtarea, "Pendiente", "0", "1")
                } finally {
                    try {
                        fg.ins_historial(requireContext(), motivo.text.toString(), latact, lonact)
                    } finally {
                        try {
                            lts.historynotes(requireContext(), motivo.text.toString())
                        } finally {
                            findNavController().navigate(R.id.navigation_map)
                            popupWindow.dismiss()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(),
                    "Debe escribir una observacion para continuar",
                    Toast.LENGTH_LONG).show()
            }
        }
        popupWindow.height = ViewGroup.LayoutParams.MATCH_PARENT
        popupWindow.showAtLocation(popUp, Gravity.CENTER, 0, 0)
    }

    private fun openformaFragment() {
        val fg = FuncionesGenerales()
        val tipoform = fg.parametro(requireContext(), "TIPO_GRP")
        val bk = Backups()
        bk.backupdDatabase(requireContext())
        if (tipoform == "0") {
            findNavController().navigate(R.id.formaFragment)
        } else {
            if (fg.standby_actual(requireContext(), fg.tarea_act(requireContext()))
                    .toString() == "1" || fg.estado_act(requireContext()) == "En Proceso"
            ) {
                findNavController().navigate(R.id.formaFragment)
            } else {
                val inflater =
                    requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                popupEstadoTarea(inflater)
            }
        }
    }

    private fun popupEstadoTarea(inflater: LayoutInflater) {
        val fg = FuncionesGenerales()
        val conGen = ConsultaGeneral()
        val ce = CargarEstados()
        val bk = Backups()
        bk.backupdDatabase(requireContext())
        var canitems = 0
        var spincoditems: Array<String?>
        var spintxtitems: Array<String?>
        val popUp = inflater.inflate(R.layout.valexitoso_layout, null)
        val pregunta = popUp.findViewById<View>(R.id.tvPregunta) as TextView
        val etObserv = popUp.findViewById<View>(R.id.et_ObservNoExit) as EditText
        val finalizar = popUp.findViewById<View>(R.id.btnFinalizar) as TextView
        val spinnoexit = popUp.findViewById<View>(R.id.spinnerNoExitoso) as Spinner
        val popupWindow = PopupWindow(popUp, LinearLayout.LayoutParams.MATCH_PARENT, 500, true)
        var posicion = "0"
        val querySpin =
            "SELECT CAST(idnoexitoso as INTEGER) as idne,noexitoso FROM '105_NO_EXITOSO' order by 1 asc"
        val objSpin =
            conGen.queryObjeto2val(requireContext(), querySpin, null)
        if (objSpin.isNotEmpty()) {
            spincoditems = arrayOfNulls(objSpin.size + 1)
            spintxtitems = arrayOfNulls(objSpin.size + 1)
            spincoditems[canitems] = "0"
            spintxtitems[canitems] = "Seleccione una opcion"
            canitems++
            for (op in objSpin.indices) {
                spincoditems[canitems] = objSpin[op][0]
                spintxtitems[canitems] = objSpin[op][1]
                canitems++
            }
            val aa = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                spintxtitems
            )
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            //  SET EL ADAPTADOR DENTRO DEL SPINNER
            spinnoexit.setAdapter(aa)
            spinnoexit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    if (position != 0) {
                        posicion = spincoditems[position].toString()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            finalizar.setOnClickListener {
                val observacionnoexit = etObserv.text.toString()
                if (observacionnoexit.length > 4 && posicion.toInt() > 0) {
                    fg.act_param(requireContext(), "NOVEDAD", "1")
                    fg.act_param(
                        requireContext(),
                        "IDNOVEDAD",
                        spincoditems[posicion.toInt()].toString()
                    )
                    fg.act_estadotarea(requireContext())
                    ce.act_Exitosa_NoExitosa(
                        requireContext(), fg.parametro(
                            requireContext(),
                            "EXI_NOEXIT"
                        ), spincoditems[posicion.toInt()].toString()
                    )
                    val lts = LoadTaskSqlite()
                    lts.historynotes(requireContext(), observacionnoexit)
                    findNavController().navigate(R.id.navigation_map)
                    popupWindow.dismiss()
                } else {
                    Toast.makeText(requireContext(),
                        "Debe Seleccionar un motivo  y escribir una observacion para continuar",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
        val cancel = popUp.findViewById<View>(R.id.btnNoExitoso) as TextView
        val ok = popUp.findViewById<View>(R.id.btnExitoso) as TextView
        val cancelar = popUp.findViewById<View>(R.id.btnCancelar) as TextView
        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
        var latact = latitud_ult
        var lonact = longitud_ult

        cancel.setOnClickListener {
            fg.act_param(requireContext(), "EXI_NOEXIT", "2")
            val preg = "Seleccione el motivo de porque la tarea no es exitosa"
            pregunta.text = preg
            cancel.visibility = View.GONE
            ok.visibility = View.GONE
            finalizar.visibility = View.VISIBLE
            etObserv.visibility = View.VISIBLE
            spinnoexit.visibility = View.VISIBLE
            fg.act_param(requireContext(), "ESTADO_ACT", "No Exitosa")
            fg.ins_historial(requireContext(), "0", latact, lonact)
            fg.act_estadotarea(requireContext())
        }
        ok.setOnClickListener {
            try {
                fg.act_param(requireContext(), "NOVEDAD", "0")
                fg.act_param(requireContext(), "IDNOVEDAD", "0")
                fg.act_param(requireContext(), "ESTADO_ACT", "En Proceso")
                fg.act_param(requireContext(), "EXI_NOEXIT", "1")
                fg.act_estadotarea(requireContext())
                fg.ins_historial(requireContext(), "0", latact, lonact)
            } finally {
                findNavController().navigate(R.id.formaFragment)
                popupWindow.dismiss()
            }
        }
        cancelar.setOnClickListener {
            fg.act_param(requireContext(), "EXI_NOEXIT", "0")
            fg.act_param(requireContext(), "ESTADO_ACT", "Pendiente")
            fg.ins_historial(requireContext(), "0", latact, lonact)
            fg.act_estadotarea(requireContext())
            popupWindow.dismiss()
        }
        popupWindow.height = ViewGroup.LayoutParams.MATCH_PARENT
        popupWindow.showAtLocation(popUp, Gravity.CENTER, 0, 0)
    }

    fun getPostData(params: MutableMap<String, String>, context: android.content.Context?) {
        if (!Common.isConnected) {
            var listImage: java.util.ArrayList<MutableMap<String, String>?>?
            try {
                listImage =
                    Paper.book()
                        .read<java.util.ArrayList<MutableMap<String, String>?>>("listEmail")
                if (listImage == null) listImage =
                    java.util.ArrayList<MutableMap<String, String>?>() else {
                }
            } catch (e: java.lang.Exception) {
                listImage = java.util.ArrayList<MutableMap<String, String>?>()
            }
            listImage!!.add(params)
            Paper.book().write("listEmail", listImage)
        } else {
            val queue = Volley.newRequestQueue(context)
            // Request a string response from the provided URL.
            val stringRequest: StringRequest =
                object : StringRequest(Method.POST, Common.urlApi,
                    Response.Listener<String?> {
                    }, Response.ErrorListener {
                    }) {
                    override fun getParams(): MutableMap<String, String> {
                        return params
                    }
                }
            // Add the request to the RequestQueue.
            queue.add<String>(stringRequest)
        }
    }

    private fun getDirection() {
        val fg = FuncionesGenerales()
        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
        val latdbl = latitud_ult.toDouble()
        val londbl = longitud_ult.toDouble()
        if (!Common.isConnected) {
            disMin = (Tools.calcCrow(latdbl as Double,
                londbl as Double,
                riderLatDest!!.toDouble(),
                riderLngDest!!.toDouble()
            ) * 1000)
            val horaLlegada = Date()
            horaLlegada.year = horaLlegada.year + 1900
            horaLlegada.minutes = (horaLlegada.minutes + disMin).toInt()
            txtDist?.text = disMin.toString() + ""
            txtTimeEst?.text = (disMin / 210).toString() + ""
        } else {
            val queue = Volley.newRequestQueue(requireContext())
            // Request a string response from the provided URL.
            val stringRequest = StringRequest(com.android.volley.Request.Method.POST,
                ("https://maps.googleapis.com/maps/api/directions/json?" +
                        "mode=driving&" +
                        "departure_time=now&" +
                        "transit_routing_preference=less_driving&" +
                        "origin=" + latdbl + "," + londbl + "&" +
                        "destination=" + riderLatDest + "," + riderLngDest + "&" +
                        "key=AIzaSyCmtQfrrX6NGPMWoSOqlHi7PsQ0epqGEBo"),
                { response ->
                    try {
                        val gson = Gson()
                        val objResponse = gson.fromJson(response, GoogleApi::class.java)
                        val routes = objResponse.routes
                        for (route in routes) {
                            val path: MutableList<MutableMap<String, Any>> =
                                java.util.ArrayList()
                            val legs = route.legs
                            for (leg in legs) {
                                val steps = leg.steps
                                for (step in steps) {
                                    var polyline = ""
                                    polyline = step.polyline.points
                                    val list: List<LatLng>? = Tools.decodePoly(polyline)
                                    if (list != null) {
                                        for (polylineD in list) {
                                            val hm = java.util.HashMap<String, Any>()
                                            hm["lat"] = polylineD.latitude.toString()
                                            hm["lng"] = polylineD.longitude.toString()
                                            path.add(hm)
                                        }
                                    }
                                }
                                val routesD: MutableList<MutableList<MutableMap<String, Any>>> =
                                    arrayListOf()
                                routesD.add(path)
                                generateRute(routesD)
                            }
                        }
                        val leg = objResponse.routes[0].legs[0]
                        val distance = leg.distance
                        startDir = leg.start_address
                        val distanceText = distance.text
                        val durationText = leg.duration.text
                        txtDist?.text = distanceText
                        txtTimeEst?.text = durationText
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(),
                            getString(R.string.error_charge_rute),
                            Toast.LENGTH_SHORT).show()
                    }
                }) { error ->
                error.printStackTrace()
                Toast.makeText(requireContext(),
                    getString(R.string.problem_conexion),
                    Toast.LENGTH_LONG).show()
            }

            // Add the request to the RequestQueue.
            queue.add(stringRequest)
        }
        if (idtarea != "" && idtarea != "0" && idtarea != null) fg.act_estadotareac(requireContext(),
            idtarea,
            "En Recorrido")
    }

    private fun generateRute(routes: MutableList<MutableList<MutableMap<String, Any>>>) {
        val points: java.util.ArrayList<LatLng?> = java.util.ArrayList()
        val lineOptions = PolylineOptions()
        val routePosition = routes[0]

        // recorriendo todas las rutas
        for (ubication in routePosition) {
            val lat = ubication["lat"]?.toString()?.toDouble() ?: 0.0
            val lng = ubication["lng"]?.toString()?.toDouble() ?: 0.0
            val position = LatLng(lat, lng)
            points.add(position)
        }
        lineOptions.addAll(points)
        lineOptions.width(5f)
        lineOptions.color(Color.RED)
        lineOptions.geodesic(true)
        direction?.remove()
        direction = mMap.addPolyline(lineOptions)
    }

    //Trazado de ruta en el mapa
    private fun getDirections() {
        val fg = FuncionesGenerales()
        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
        val latdbl = latitud_ult.toDouble()
        val londbl = longitud_ult.toDouble()
        if (arrayService.size != 0) {
            val points: MutableList<LatLng> = ArrayList()
            val polylineOptions = PolylineOptions()
            val lt = LatLng(latdbl, londbl)
            points.add(lt)
            for (i in arrayService.indices) {
                val mapLatLng = arrayService.get(i).get("latLng") as MutableMap<String, Any>
                points.add(LatLng(mapLatLng["latitude"].toString().toDouble(),
                    mapLatLng["longitude"].toString().toDouble()))
            }
            txtTimeEst?.text = "00:00 min"
            txtDist?.text = "0 km"
            polylineOptions.addAll(points)
            polylineOptions.width(5f)
            polylineOptions.color(Color.RED)
            polylineOptions.geodesic(true)
            direction?.remove()
            direction = mMap.addPolyline(polylineOptions)
        } else {
            Toast.makeText(requireContext(), "Sin Tareas", Toast.LENGTH_SHORT).show()
        }
    }

    private fun starLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
            mLocationRequest,
            this)
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child(
            Common.documentUser!!.get(
                "typeVeh"
            ).toString() + "/" + FirebaseAuth.getInstance().currentUser!!.uid
        ).removeValue()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
        val fg = FuncionesGenerales()
        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
        val latdbl = latitud_ult.toDouble()
        val londbl = longitud_ult.toDouble()
        val updateRegister: MutableMap<String, Any> = java.util.HashMap<String, Any>()
        updateRegister["Estado"] = "offLine"
        FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child(
            Common.documentUser!!.get(
                "typeVeh"
            ).toString() + "/" + FirebaseAuth.getInstance().currentUser!!.uid
        ).updateChildren(updateRegister)
        try {
            Paper.book().write(
                "location", LatLng(latdbl, londbl)
            )
            val mLocation = Paper.book().read<LatLng>("location")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    override fun onLocationChanged(location: android.location.Location) {
        Common.mLastLocation = location
        val updateRegister2: MutableMap<String, Any> = java.util.HashMap<String, Any>()
        updateRegister2["0"] = location.latitude
        updateRegister2["1"] = location.longitude
        val updateRegister: MutableMap<String, Any> = java.util.HashMap<String, Any>()
        updateRegister["l"] = updateRegister2
        updateRegister["Estado"] = "Line"
        try {
            updateRegister["typeVeh"] = Common.documentUser!!.get("typeVeh").toString()
        } catch (e: java.lang.Exception) {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        try {
            FirebaseDatabase.getInstance().getReference(Common.driver_tb1)
                .child(
                    Common.documentUser!!.get("typeVeh")
                        .toString() + "/" + FirebaseAuth.getInstance().currentUser!!.uid
                )
                .updateChildren(updateRegister)
        } catch (e: java.lang.Exception) {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        if (Common.boolRecKm) {
            val fg = FuncionesGenerales()
            try {
                val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
                val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
                val latdbl = latitud_ult.toDouble()
                val londbl = longitud_ult.toDouble()
                val mtRec = Paper.book().read<Double>("mtRec")
                val mtRec2 = mtRec + Tools.calcCrow(latdbl as Double,
                    londbl as Double, location.latitude, location.longitude) * 1000
                Paper.book().write("mtRec", mtRec2)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        displayLocation()
    }

}