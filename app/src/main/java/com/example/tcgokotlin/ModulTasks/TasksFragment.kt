package com.example.tcgokotlin.ModulTasks

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.tcgokotlin.Helper.GPSTracker
import com.example.tcgokotlin.Helper.Tools
import com.example.tcgokotlin.Helper.vo.Resource
import com.example.tcgokotlin.ModulMain.MainActivity
import com.example.tcgokotlin.R
import com.example.tcgokotlin.Sincronizar.CargarEstados
import com.example.tcgokotlin.Sincronizar.SincronizarOnline
import com.example.tcgokotlin.data.model.History
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import io.paperdb.Paper
import kotlinx.android.synthetic.main.layout_info_task.*
import kotlinx.android.synthetic.main.layout_list_rutes.*
import java.lang.reflect.TypeVariable
import java.text.ParseException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class TasksFragment : Fragment() {

    var estadotask: String = ""
    var riderLatDest: String? = null
    var riderLngDest: String? = null

    var customerId: String? = null
    var idServicio: String? = null
    var servicio: MutableMap<String, Any>? = null
    var idtarea: String = ""
    var estadorecant = ""
    private lateinit var tareasact: Array<String?>

    private val viewModelActivity by activityViewModels<MainViewModel> {
        MainViewModelFactory(
            UseCase(Repo())
        )
    }

    var waitingDialog: AlertDialog? = null

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var gpsInfo: GPSTracker

    var storage = FirebaseStorage.getInstance()
    var storageRef = storage.reference.child("images")
    var menuTasks: Menu? = null

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_close_anim
        )
    }
    private val fromRight: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.from_right_anim
        )
    }
    private val toRight: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.to_right_anim
        )
    }

    private var clicked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Common.ifJornada != 1) {
            Toast.makeText(
                requireActivity(),
                getString(R.string.must_init_forst_workday),
                Toast.LENGTH_SHORT
            ).show()
            requireActivity().startActivity(Intent(requireActivity(), MainActivity::class.java))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        setHasOptionsMenu(true)
        if ((activity as AppCompatActivity).supportActionBar != null) {
            (activity as AppCompatActivity).supportActionBar?.show()
        }
        return inflater.inflate(R.layout.layout_list_rutes, container, false)

    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasks_menu, menu)
        menuTasks = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_my_tasks -> {
                showListTasks()
                true
            }
            R.id.action_finish_tasks -> {
                showListTasksFinish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fg = FuncionesGenerales()
        Paper.init(requireContext())
        gpsInfo = GPSTracker(requireContext())
        Common.mLastLocation = gpsInfo.currentLocation
        if (Common.db == null) Common.setDbSettings()
        Common.mContext = requireContext()
        Common.mLayoutInflater = this.layoutInflater
        Common.waitingDialog = SpotsDialog.Builder().setContext(requireContext()).build()
        setListeners()
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
        showHojaRuta(view)
    }

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

                        sm.setIntTasks(result.data?.size ?: 0)
                    }
                    if (sm.getIntTasks() < result.data?.size ?: 0) {
                        sm.setIntTasks(result.data?.size ?: 0)

                        sm.setIntTasks(arrayServiceConsult.size)
                    }
                    if (sm.getIntTasks() < arrayServiceConsult.size) {
                        sm.setIntTasks(arrayServiceConsult.size)
                        mediaPlayer?.start()
                        arrayService = Tools.chargeArrayService()
                        val mAdapter = ListAdapterRutes(requireContext(), arrayService, 0)
                        listRutes.adapter = mAdapter
                        showListTasks()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.do_you_have_new_tasks),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                is Resource.Failure -> {
                    Toast.makeText(
                        requireContext(),
                        "Ocurrio un error ${result.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun setListeners() {
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.tarea)
        mediaPlayer?.isLooping = false
        observeServicesPen1()
        fbMain.setOnClickListener {
            onAddButtonClicked()
        }
        fbDistance.setOnClickListener {
            Common.ifRute = false
            view?.let { organizeRuta(it, true) }
        }
        fbTime.setOnClickListener {
            Common.ifRute = true
            view?.let { organizeRuta(it, true) }
        }
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            fbTime.startAnimation(fromRight)
            fbDistance.startAnimation(fromRight)
            fbMain.startAnimation(rotateOpen)
        } else {
            fbTime.startAnimation(toRight)
            fbDistance.startAnimation(toRight)
            fbMain.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            fbDistance.visibility = View.VISIBLE
            fbTime.visibility = View.VISIBLE
        } else {
            fbDistance.visibility = View.INVISIBLE
            fbTime.visibility = View.INVISIBLE
        }
    }

    private fun showHojaRuta(view: View) {
        val fg = FuncionesGenerales()
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
        organizeRuta(view, false)
    }

    fun verificarArray2(i: Int, j: Int) {
        val fg = FuncionesGenerales()
        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
        val latdbl = latitud_ult.toDouble()
        val londbl = longitud_ult.toDouble()
        disMin = if (i == 0) {
            Tools.calcCrow(
                latdbl as Double, londbl as Double,
                Tools.createMutableMap(j, arrayService)["latitude"].toString().toDouble(),
                Tools.createMutableMap(j, arrayService)["longitude"].toString().toDouble()
            ) * 1000 / 210
        } else {
            Tools.calcCrow(
                Tools.createMutableMap(i - 1, arrayService)["latitude"].toString().toDouble(),
                Tools.createMutableMap(i - 1, arrayService)["longitude"].toString().toDouble(),
                Tools.createMutableMap(j, arrayService)["latitude"].toString().toDouble(),
                Tools.createMutableMap(j, arrayService)["longitude"].toString().toDouble()
            ) * 1000 / 210
        }
        if (for1) {
            rango = disMin
            for1 = false
        }
        if (disMin < rango) {
            rango = disMin
            temp = arrayService[j]
            arrayService[j] = arrayService[i]
            arrayService[i] = temp!!
        }
    }

    var alertDialog: androidx.appcompat.app.AlertDialog.Builder? = null
    var show: androidx.appcompat.app.AlertDialog? = null
    var horaNow: Date? = null
    var horaRange: Date? = null
    var ruteRange = 0
    var ruteCerca = 0
    private fun organizeRuta(view: View, options: Boolean) {
        contVip = -1
        contFin = arrayService.size
        val horaNow = Date()
        horaNow.year = horaNow.year + 1900
        val horaMin = Date()
        horaMin.year = horaMin.year + 1900
        horaMin.minutes = horaMin.minutes + 30

        //fg.ejecDB(requireContext(),"INSERT OR IGNORE INTO params (pa) values ('USER') , ('PASS') , ('VERSION') , ('ULTIMAP') , ('TAREA_ACT'), ('CLIENTE_ACT') , ('FORMA_ACT') , ('GRUPO_ACT')  , ('SUBGRUPO_ACT') , ('PREGUNTA_ACT') , ('IDDRIVER'), ('IDFOTO_ACT') , ('TOTALFOTOSMAX') ")

        /* *
         * Mover las tareas VIP al principio de la ruta si falta media hora o menos para ejecutarce
         * y mover las tareas que ya pasaron la hora de ejecución al final
         */
        var i = 0
        while (i < arrayService.size) {
            try {
                val mapService = arrayService[i]
                val horaTareaIn = Date()
                horaTareaIn.year = horaTareaIn.year + 1900
                val horaTareaFin = Date()
                horaTareaFin.year = horaTareaFin.year + 1900
                val mapHistory =
                    (mapService["historial"] as HashMap<String, Any>)[Common.formatDate.format(Date())] as Map<String, Any>?


                horaTareaIn.hours =
                    Common.formatHora.parse(mapHistory?.get("TareaRangoIn")?.toString()).hours
                horaTareaIn.hours =
                    Common.formatHora.parse(mapHistory?.get("TareaRangoIn")?.toString()).minutes
                horaTareaFin.hours =
                    Common.formatHora.parse(mapHistory?.get("TareaRangoFin")?.toString()).hours
                horaTareaFin.hours =
                    Common.formatHora.parse(mapHistory?.get("TareaRangoFin")?.toString()).minutes
                if (horaTareaFin.time < horaNow.time) {
                    if (i == contFin) {
                        i = arrayService.size
                    } else {
                        contFin--
                        temp = mapService
                        arrayService[i] = arrayService[contFin]
                        arrayService[contFin] = temp!!
                        i--
                    }
                } else if (mapService["prioridad"] == "VIP" && horaTareaIn.time <= horaMin.time) {
                    contVip++
                    temp = mapService
                    arrayService[i] = arrayService[contVip]
                    arrayService[contVip] = temp!!
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            i++
        }

        /* *
         * Organizar por distancia y hora las tareas
         */
        i = 0
        for (i in arrayService.indices) {
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
            var j = i
            while (j in (contVip + 1) until contFin) {
                if (Common.ifRute) {
                    verificarArray(i, j, 2)
                } else {
                    verificarArray2(i, j)
                }
                j++
            }
            j = i
            while (j >= contFin && j < arrayService.size - 1) {
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
            if (arrayService[i]["estado"] == "En Proceso") {
                positionSelected = i
            }
            try {
                val mapService = arrayService[i]
                val mapHistory =
                    (mapService["historial"] as Map<*, *>?)!![Common.formatDate.format(Date())] as Map<*, *>?
                val dateNow = Date()
                dateNow.year = dateNow.year + 1900
                dateNow.hours = dateNow.hours + 2
                val horaTareaIn = Date()
                horaTareaIn.year = horaTareaIn.year + 1900
                horaTareaIn.hours =
                    Common.formatHora.parse(mapHistory!!["TareaRangoIn"].toString()).hours
                horaTareaIn.minutes =
                    Common.formatHora.parse(mapHistory["TareaRangoIn"].toString()).minutes
                if (horaTareaIn.time <= dateNow.time) {
                    ruteRange = i
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        for (i in 0..ruteRange) {
            distanceRuta = 0.0
            if (arrayService.size != 0) {
                try {
                    val mapLatLng = arrayService[i]["latLng"] as Map<*, *>?
                    getDistancia(
                        i,
                        mapLatLng!!["latitude"].toString().toDouble(),
                        mapLatLng["longitude"].toString().toDouble(),
                        view
                    )
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else {
                waitingDialog?.dismiss()
                showLayoutNoResults()
                Toast.makeText(requireContext(), getString(R.string.no_tasks), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        if (options) {
            showListTasks()
        }
        try {
            show?.dismiss()
            waitingDialog?.dismiss()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun showLayoutNoResults() {
        tvTitleList.visibility = View.GONE
        clWithoutTasks.visibility = View.VISIBLE
        tvNotResults.text = getString(R.string.not_taks_available)
        listRutes.visibility = View.GONE
    }

    private fun showLayoutNoResultsFinish() {
        tvTitleList.visibility = View.GONE
        clWithoutTasks.visibility = View.VISIBLE
        tvNotResults.text = getString(R.string.no_has_task_finish)
        listRutesFin.visibility = View.GONE
    }

    var contVip = -1
    var contFin = 0
    var temp: MutableMap<String, Any>? = null
    var for1 = true
    var for2: Boolean = true
    var for3: Boolean = true
    var disMin = 0.0
    var rango: Double = 0.0
    var rango3: Double = 0.0
    fun verificarArray(i: Int, j: Int, mFor: Int) {
        val fg = FuncionesGenerales()
        val mapHistory =
            (arrayService[j]["historial"] as MutableMap<*, *>?)!![Common.formatDate.format(Date())] as MutableMap<*, *>?
        val mapLatLng = arrayService[j]["latLng"] as MutableMap<*, *>?
        try {
            val horaTareaFin = Date()
            horaTareaFin.year = horaTareaFin.year + 1900
            horaTareaFin.hours =
                Common.formatHora.parse(mapHistory!!["TareaRangoFin"].toString()).hours
            horaTareaFin.minutes =
                Common.formatHora.parse(mapHistory["TareaRangoFin"].toString()).minutes
            val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
            val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
            val latdbl = latitud_ult.toDouble()
            val londbl = longitud_ult.toDouble()
            disMin = if (i == 0) {
                Tools.calcCrow(
                    latdbl as Double,
                    londbl as Double, mapLatLng?.get("latitude")?.toString()?.toDouble()
                        ?: 0.0, mapLatLng?.get("longitude")?.toString()?.toDouble() ?: 0.0
                ) * 1000 / 210
            } else {
                val mapLatLng2 = arrayService[i - 1]["latLng"] as Map<*, *>?
                Tools.calcCrow(
                    mapLatLng2?.get("latitude").toString().toDouble(),
                    mapLatLng2?.get("longitude").toString().toDouble(),
                    mapLatLng?.get("latitude").toString().toDouble(),
                    mapLatLng?.get("longitude").toString().toDouble()
                ) * 1000 / 210
            }
            val horaLlegada = Date()
            horaLlegada.year = horaLlegada.year + 1900
            horaLlegada.minutes = (horaLlegada.minutes + disMin).toInt()
            val rango2 = (horaTareaFin.time - horaLlegada.time).toDouble()
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
                    temp = arrayService[j]
                    arrayService[j] = arrayService[i]
                    arrayService[i] = temp!!
                }
            } else {
                if (rango3 == 0.0) {
                    rango3 = rango2
                }
                if (rango2 < rango3) {
                    rango3 = rango2
                    temp = arrayService[j]
                    when (mFor) {
                        1 -> if (j != contVip) {
                            arrayService[j] = arrayService[contVip]
                            arrayService[contVip] = temp!!
                            verificarArray(i, j, 1)
                        }
                        2 -> if (j < contFin - 1) {
                            arrayService[j] = arrayService[contFin - 1]
                            arrayService[contFin - 1] = temp!!
                            verificarArray(i, j, 2)
                        }
                        3 -> if (j < arrayService.size - 1) {
                            arrayService[j] = arrayService[arrayService.size - 1]
                            arrayService[arrayService.size - 1] = temp!!
                            verificarArray(i, j, 3)
                        }
                    }
                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    var positionSelected = -1
    var distanceRuta = 0.0
    var idRuta = 0
    private fun getDistancia(index: Int, lat: Double, lon: Double, view: View) {
        val fg = FuncionesGenerales()
        try {
            val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
            val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
            val latdbl = latitud_ult.toDouble()
            val londbl = longitud_ult.toDouble()
            val metros = Tools.calcCrow(
                latdbl as Double,
                londbl as Double,
                lat,
                lon
            ) * 1000
            if (distanceRuta == 0.0) {
                distanceRuta = metros
                idRuta = index
            } else if (distanceRuta > metros) {
                distanceRuta = metros
                idRuta = index
            }
            ruteCerca++
            if (ruteCerca == ruteRange + 1) {
                arrayService[idRuta]["geocerca"] = "1"
                cargarRuta(view)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    var arrayService = ArrayList<MutableMap<String, Any>>()
    var arrayServiceConsult = ArrayList<MutableMap<String, Any>>()
    var arrayServiceFin = ArrayList<MutableMap<String, Any>>()

    private fun showListTasks() {
        listRutesFin.visibility = View.GONE
        clWithoutTasks.visibility = View.GONE
        if (arrayService.size > 0) {
            tvTitleList.text = getString(R.string.my_tasks)
            tvTitleList.visibility = View.VISIBLE
            listRutes.visibility = View.VISIBLE

            val show: androidx.appcompat.app.AlertDialog? = alertDialog?.show()
            listRutes.onItemClickListener = OnItemClickListener { parent, view, position, id ->
                showTarea(position)
                positionSelected = position
                val mapLatLng = arrayService[position]["latLng"] as Map<String, Any>
                riderLatDest = mapLatLng["latitude"].toString()
                riderLngDest = mapLatLng["longitude"].toString()
                show?.dismiss()
            }
        } else {
            showLayoutNoResults()
        }
    }

    private fun showListTasksFinish() {
        clWithoutTasks.visibility = View.GONE
        listRutes.visibility = View.GONE
        if (arrayServiceFin.size > 0) {
            tvTitleList.text = getString(R.string.tasks_finish)
            tvTitleList.visibility = View.VISIBLE
            listRutesFin.visibility = View.VISIBLE
        } else {
            showLayoutNoResultsFinish()
        }
    }

    private fun cargarRuta(v: View) {
        Common.listServicios = true
        val listRutes = v.findViewById<ListView>(R.id.listRutes)
        val listRutesFin = v.findViewById<ListView>(R.id.listRutesFin)
        val txtTipoServicio = v.findViewById<TextView>(R.id.txtTipoServicio)
        val txtCostoMonetario = v.findViewById<TextView>(R.id.txtCostoMonetario)
        val txtObservaciones = v.findViewById<TextView>(R.id.txtObservaciones)
        txtTipoServicio.text = servicio?.get("typeService")?.toString() ?: ""
        txtCostoMonetario.text = getString(R.string.no)
        txtObservaciones.text = servicio?.get("nota")?.toString() ?: ""
        listRutes.adapter = null
        listRutesFin.adapter = null
        val mAdapter = ListAdapterRutes(requireContext(), arrayService, 0)
        val mAdapterFin = ListAdapterRutes(requireContext(), arrayServiceFin, 0)
        listRutes.adapter = mAdapter
        listRutesFin.adapter = mAdapterFin
        val show: androidx.appcompat.app.AlertDialog? = alertDialog?.show()
        listRutes.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            showTarea(position)
            positionSelected = position
            val mapLatLng = arrayService[position]["latLng"] as Map<String, Any>
            riderLatDest = mapLatLng["latitude"].toString()
            riderLngDest = mapLatLng["longitude"].toString()
            show?.dismiss()
        }
    }
    var showTarea: AlertDialog? = null
    var boolFirma: kotlin.Boolean? = false
    var bool1 = 0
    var bool2: Int? = 0
    var bool3: Int? = 0
    var builder: AlertDialog.Builder? = null
    private fun showTarea(zIndex: Int): Boolean {
        val fg = FuncionesGenerales()
        positionSelected = zIndex
        Common.selectedService = arrayService[positionSelected]
        val service = arrayService[positionSelected]
        val alertDialog = AlertDialog.Builder(requireContext())
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
        val btnInFinTarea = validate_info_task.findViewById<MaterialButton>(R.id.btnInFinTarea)
        val btnInRecTarea = validate_info_task.findViewById<Button>(R.id.btnInRecTarea)
        val btnStandBy = validate_info_task.findViewById<TextView>(R.id.btnStandBy)
        val btnInFinTiempoMuerto =
            validate_info_task.findViewById<Button>(R.id.btnInFinTiempoMuerto)
        val showListTasks = validate_info_task.findViewById<Button>(R.id.btnShowTaskMap)
        idtarea = arrayService[zIndex]["codetarea"].toString()
        val fa = fg.fechaActual(1)
        val crerec =  fg.getQ1(requireContext(),
            "Select ifnull(count(*),'0') as crep from '200_TAREAS' where estado='En Recorrido' and (activa='1' or abierta='1')")
        if (crerec.toInt()>1){
            val maxidhisttare = fg.getQ1(requireContext(),"select max(idhistorial) from '102_HISTORIAL' where fecha='$fa'  and idtarea='$idtarea'")
            val cterest1 = fg.getQ1(requireContext(),"select estadorec from '102_historial' where fecha='$fa' and idtarea='$idtarea' and idhistorial='$maxidhisttare'")
            if (cterest1 == "1"){
                fg.ejecDB(requireContext(),"update '200_TAREAS' set estado='Pendiente' where fecha='$fa' and idtarea='$idtarea'")
                val ce = CargarEstados()
                ce.ActualizarServicesp(requireContext(),idtarea)
            }
        }
        fg.act_param(requireContext(), "TAREA_ACT", idtarea)
        val tarenproc = fg.getQ1(requireContext(),
            "Select ifnull(count(*),'0') as crep from '200_TAREAS' where estado='En Proceso' and (activa='1' or abierta='1')")
        if (tarenproc != "0") {
            fg.act_param(requireContext(), "ESTADOREC_ACT", "2")
        }
        val tarealat = fg.getlatp(requireContext(), idtarea)
        val tarealon = fg.getlonp(requireContext(), idtarea)
        val met = fg.parametro(requireContext(), "RANGOMTS").toInt()


        estadotask = fg.getQ1(
            requireContext(),
            "Select estado from '200_TAREAS' where idtarea='" + idtarea + "'"
        )
        val tarea_StandBy = fg.standby_actual(requireContext(), idtarea)
            .toString()
        fg.act_param(requireContext(), "ESTADO_ACT", estadotask.toString())
        showViewAndButton(showListTasks)
        showListTasks.setOnClickListener {
            val map = service["latLng"] as MutableMap<String, Any>
            val latLngTask = LatLng(
                map["latitude"].toString().toDouble(),
                map["longitude"].toString().toDouble()
            )
            showTarea?.dismiss()
            openFragmentWithLocation(latLngTask)
        }

        val ntarea =
            fg.getQ1(
                requireContext(),
                "SELECT tarea from '200_TAREAS' where idtarea='" + idtarea + "'"
            ).toString()
        val idformulario =
            fg.getQ1(
                requireContext(),
                "SELECT idformulario from '108_FORMULARIOS' where idtarea='" + idtarea + "'"
            )
                .toString()
        val nformulario =
            fg.getQ1(
                requireContext(),
                "SELECT formulario from '108_FORMULARIOS' where idtarea='" + idtarea + "'"
            )
                .toString()
        val tipotarea = arrayService[zIndex]["tipoTarea"].toString()
        txtId.text = idtarea.toString().substring(11)

        fg.act_param(requireContext(), "NTAREA_ACT", ntarea)
        fg.act_param(requireContext(), "FORMULARIO_ACT", idformulario)
        fg.act_param(requireContext(), "NFORMULARIO_ACT", nformulario)
        fg.act_param(requireContext(), "TIPO_GRP", tipotarea)
        val estadotarea = fg.getQ1(
            requireContext(),
            "SELECT estado from '200_TAREAS' WHERE idtarea='" + idtarea + "'"
        )
        if (estadotarea.equals("Finalizado")) {
            fg.ejecDB(
                requireContext(),
                "DELETE FROM '301_RESPUESTAS_TEMP' WHERE idtarea='" + idtarea + "';"
            )
            fg.ejecDB(
                requireContext(),
                "DELETE FROM '302_FOTOS_RESP_TEMP' WHERE idtarea='" + idtarea + "';"
            )
            fg.ejecDB(
                requireContext(),
                "INSERT OR IGNORE INTO '301_RESPUESTAS_TEMP'(idtarea,idformulario,idform,grpid,sgrpid,idpreg,preg,opn,opt,rta1,rta2,obs,gpslat,gpslon,useruid) SELECT idtarea,idformulario,idform,grpid,sgrpid,idpreg,preg,opn,opt,rta1,rta2,obs,gpslat,gpslon,useruid from '301_RESPUESTAS' WHERE idtarea='" + idtarea + "' order by 1,2,3,4,5,6 ASC;"
            )
            fg.ejecDB(
                requireContext(),
                "INSERT OR IGNORE INTO '302_FOTOS_RESP_TEMP'(idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,idfoto,nfoto,gpslat,gpslon,useruid) SELECT idtarea,idformulario,idform,grpid,sgrpid,idpreg,opn,idfoto,nfoto,gpslat,gpslon,useruid from '302_FOTOS_RESP' WHERE idtarea='" + idtarea + "' order by 1,2,3,4,5,6 ASC;"
            )
        } else {
            fg.ejecDB(
                requireContext(),
                "INSERT OR IGNORE INTO '301_RESPUESTAS_TEMP'(idtarea,idformulario,idform,grpid,sgrpid,idpreg,preg,opn,opt) SELECT '204_PREGGEN'.idtarea , '204_PREGGEN'.idformulario , '204_PREGGEN'.idform ,'204_PREGGEN'.grpid,'204_PREGGEN'.sgrpid,'204_PREGGEN'.idpreg,'204_PREGGEN'.preg,'0' as opn,'0' as opt from '204_PREGGEN' LEFT JOIN '301_RESPUESTAS_TEMP' ON '204_PREGGEN'.idtarea = '301_RESPUESTAS_TEMP'.idtarea and  '204_PREGGEN'.idformulario = '301_RESPUESTAS_TEMP'.idformulario and  '204_PREGGEN'.idform = '301_RESPUESTAS_TEMP'.idform and  '204_PREGGEN'.grpid = '301_RESPUESTAS_TEMP'.grpid and  '204_PREGGEN'.sgrpid = '301_RESPUESTAS_TEMP'.sgrpid and  '204_PREGGEN'.idpreg = '301_RESPUESTAS_TEMP'.idpreg  WHERE '204_PREGGEN'.idtarea='" + idtarea + "' and '204_PREGGEN'.idform<>'9991' and '204_PREGGEN'.idform<>'9992' and ('301_RESPUESTAS_TEMP'.idtarea  is null or  '301_RESPUESTAS_TEMP'.idformulario  is null or '301_RESPUESTAS_TEMP'.idform  is null or '301_RESPUESTAS_TEMP'.grpid is null or '301_RESPUESTAS_TEMP'.sgrpid is null or '301_RESPUESTAS_TEMP'.idpreg is null) order by 1,2,3,4,5,6 ASC;"
            )
        }

        txtId.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.code_copy), Toast.LENGTH_SHORT)
                .show()
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", txtId.text)
            clipboard.setPrimaryClip(clip)
        }
        val mapHistory =
            (arrayService[zIndex]["historial"] as Map<String?, Any?>)[Common.formatDate.format(Date())] as Map<String, Any>?
        val mapInfoCliente = arrayService[zIndex]["infoCliente"] as Map<String, Any>
        val mapLatLng = arrayService[zIndex]["latLng"] as Map<String, Any>
        txtEstado.setText(estadotask.toString())
        txtDateRange.text =
            mapHistory!!["TareaRangoIn"].toString() + " - " + mapHistory["TareaRangoFin"]
        fg.ejecDB(
            requireContext(),
            "UPDATE '200_TAREAS' set estimado='" + mapHistory!!["TareaTiempoEst"].toString() + "' , horario='" + mapHistory!!["TareaRangoIn"].toString() + " - " + mapHistory["TareaRangoFin"] + "' where idtarea='" + idtarea + "'"
        )
        txtTimeEstimado.text = mapHistory["TareaTiempoEst"].toString()
        txtTypeService.text = fg.typeservice(requireContext())
        txtPiezas.setText(arrayService[zIndex]["piezaCant"].toString())
        txtNomClient.text = mapInfoCliente["nomClient"].toString()
        txtDirection.text =
            "${getString(R.string.direction_spanish)}: \n${mapInfoCliente["direction"].toString()}"
        txtTel.text = mapInfoCliente["tel1"].toString() + " - " + mapInfoCliente["tel2"].toString()
        txtNotas.setText(arrayService[zIndex]["nota"].toString())
        if (arrayService[zIndex]["prioridad"] == "VIP") imgPrioridad.visibility = View.VISIBLE
        if (arrayService[zIndex]["standBy"] == "1" && arrayService[zIndex]["estado"].toString() == "Pendiente") {
            txtStandBy.visibility = View.VISIBLE
        }
        //val locser = LocationServices()
        //var gps: DoubleArray = locser.obtenerloc(requireContext())
        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
        val latdbl = latitud_ult.toDouble()
        val londbl = longitud_ult.toDouble()
        val metros = Tools.calcCrow(
            latdbl as Double,
            londbl as Double,
            mapLatLng["latitude"].toString().toDouble(),
            mapLatLng["longitude"].toString().toDouble()
        ) * 1000
        val formatDecimal = java.text.DecimalFormat("#.00")
        val distancia = metros / 1000
        if (distancia <= 1) {
            txtMetrosTarea.text = "${formatDecimal.format(distancia * 1000)}  Mts"
        } else {
            txtMetrosTarea.text = formatDecimal.format(distancia) + " Km"
        }
        hideViewAndButton(btnInFinTarea)
        hideViewAndButton(btnInRecTarea)
        hideViewAndButton(btnInFinTiempoMuerto)
        btnStandBy.visibility = View.GONE

        val tep = fg.haytarproceso(requireContext())
        val taep = fg.tarprocesoact(requireContext())
        val ter = fg.haytarrecorrido(requireContext())
        val taer = fg.tarrecorridoact(requireContext())
        val taes = fg.tarstandbyact(requireContext())

        val estadorec = fg.estadorec_act(requireContext())
        estadorecant = estadorec
        if (tep == "0" || taep == "1") {
            when (estadorec.toString()) {
                "0", "5" -> if (metros < met) {
                    if(taes=="1"){
                        btnInFinTarea.text = "FINALIZAR TAREA"
                    }
                    showViewAndButton(btnInFinTarea)
                } else {
                    if (taer == "1") {
                    btnInRecTarea.text = "CANCELAR RECORRIDO"
                }
                    btnInRecTarea.visibility = View.VISIBLE
                }
                "1", "4" -> if (taer == "1") {
                    if (metros < met) {
                        showViewAndButton(btnInFinTarea)
                        showViewAndButton(btnInFinTiempoMuerto)
                    } else {
                        btnInRecTarea.text = getString(R.string.CANCEL_TOUR)
                        showViewAndButton(btnInRecTarea)
                    }
                } else {
                    if (metros < met) {
                        showViewAndButton(btnInFinTarea)
                    } else {
                        btnInRecTarea.text = getString(R.string.REDIRECT_THIS_TASK)
                        showViewAndButton(btnInRecTarea)
                    }
                }
                "2" -> if (taep == "1") {
                    if (metros < met) {
                        btnInFinTarea.text = getString(R.string.FINISH_TASK)
                        showViewAndButton(btnInFinTarea)
                    }
                    btnStandBy.visibility = View.VISIBLE
                }
                "3" -> if (Common.recIdTarea.equals(arrayService[zIndex]["key"])) {
                    if (metros < met) {
                        showViewAndButton(btnInFinTarea)
                    }
                } else {
                    if (metros < met) {
                        showViewAndButton(btnInFinTarea)
                    } else {
                        btnInRecTarea.text = getString(R.string.REDIRECT_THIS_TASK)
                        showViewAndButton(btnInRecTarea)
                    }
                }
            }
        } else {
            showListTasks.visibility = View.GONE
        }

        alertDialog.setView(validate_info_task)
        try {
            showTarea = alertDialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        btnInRecTarea.setOnClickListener {
            try {
                showTarea?.dismiss()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            if (Common.ifAlmuerzo != 1 && Common.ifBreack != 1 && Common.ifPermiso != 1) {
                var updateInfoRed = HashMap<String, Any>()
                when (btnInRecTarea.text.toString()) {
                    getString(R.string.START_TOUR) -> {
                        try{
                            fg.act_estadotareac(requireContext(), idtarea, "En Recorrido")
                        }finally {
                            try {
                                if (estadorec.toString() == "5") {
                                        recRegistrar(1, zIndex)
                                } else {
                                        recRegistrar(1, zIndex)
                                }
                            } finally {
                                if (metros < met) {
                                    showViewAndButton(btnInFinTarea)
                                    hideViewAndButton(btnInRecTarea)
                                    showViewAndButton(btnInFinTiempoMuerto)
                                } else {
                                    hideViewAndButton(btnInFinTarea)
                                    showViewAndButton(btnInRecTarea)
                                    btnInRecTarea.text = getString(R.string.CANCEL_TOUR)
                                    hideViewAndButton(btnInFinTiempoMuerto)
                                }
                                arrayService = Tools.chargeArrayService()
                                val mAdapter = ListAdapterRutes(requireContext(), arrayService, 0)
                                listRutes.adapter = mAdapter
                            }
                        }
                    }
                    getString(R.string.REDIRECT_THIS_TASK) -> {
                        try {
                            fg.act_estadotareac(requireContext(),idtarea,"En Recorrido")
                        }finally {
                            if (estadorec.toString() == "1") {
                                recRegistrar(1, zIndex)
                            } else {
                                recRegistrar(1, zIndex)
                            }
                            if (metros < met) {
                                showViewAndButton(btnInFinTarea)
                                hideViewAndButton(btnInRecTarea)
                                showViewAndButton(btnInFinTiempoMuerto)
                            } else {
                                hideViewAndButton(btnInFinTarea)
                                showViewAndButton(btnInRecTarea)
                                btnInRecTarea.text = getString(R.string.CANCEL_TOUR)
                                hideViewAndButton(btnInFinTiempoMuerto)
                            }
                            arrayService = Tools.chargeArrayService()
                            val mAdapter = ListAdapterRutes(requireContext(), arrayService, 0)
                            listRutes.adapter = mAdapter
                        }
                    }
                    getString(R.string.CANCEL_TOUR) -> {
                        try {
                            fg.act_recorridosapend(requireContext())
                        } finally {
                            try{
                                btnInRecTarea.text = getString(R.string.START_TOUR)
                                recRegistrar(5, zIndex)

                            }finally {
                                hideViewAndButton(btnInFinTarea)
                                showViewAndButton(btnInRecTarea)
                                hideViewAndButton(btnInFinTiempoMuerto)
                                arrayService = Tools.chargeArrayService()
                                val mAdapter = ListAdapterRutes(requireContext(), arrayService, 0)
                                listRutes.adapter = mAdapter
                            }
                        }
                    }
                }
            } else {
                if (Common.ifAlmuerzo == 1) {

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_schedule_lunch),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                }
                if (Common.ifBreack == 1) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_schedule_breack),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                }
                if (Common.ifPermiso == 1) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_schedule_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_schedule_lunch),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (Common.ifBreack == 1) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_schedule_breack),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (Common.ifPermiso == 1) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_schedule_permission),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }
        btnInFinTiempoMuerto.setOnClickListener {
            if (Common.ifAlmuerzo != 1 && Common.ifBreack != 1 && Common.ifPermiso != 1) {
                recRegistrar(3, zIndex)
                showViewAndButton(btnInFinTarea)
                hideViewAndButton(btnInRecTarea)
                hideViewAndButton(btnInFinTiempoMuerto)
            } else {
                if (Common.ifAlmuerzo == 1) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_schedule_lunch),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (Common.ifBreack == 1) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_schedule_breack),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (Common.ifPermiso == 1) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_schedule_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        btnInFinTarea.setOnClickListener {
            if (Common.ifAlmuerzo != 1 && Common.ifBreack != 1 && Common.ifPermiso != 1) {
                val metros = Tools.calcCrow(
                    latdbl as Double,
                    londbl as Double,
                    mapLatLng["latitude"].toString().toDouble(),
                    mapLatLng["longitude"].toString().toDouble()
                )
                if (metros <= met) {
                    val loadtasksql = LoadTaskSqlite()
                    loadtasksql.resetParametros(requireContext())
                    fg.act_param(requireContext(), "ESTADO_ACT", "En Proceso")
                    fg.act_standby(requireContext(), "0")
                    when (btnInFinTarea.text.toString()) {
                        getString(R.string.BEGIN_TASK) -> {
                            btnInFinTarea.text = getString(R.string.FINISH_TASK)
                            recRegistrar(2, zIndex)
                            showViewAndButton(btnInFinTarea)
                            hideViewAndButton(btnInRecTarea)
                            hideViewAndButton(btnInFinTiempoMuerto)
                            btnInFinTarea.callOnClick()
                        }
                        getString(R.string.FINISH_TASK) -> {
                            showTarea?.dismiss()
                            openformaFragment()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.you_must_200_meters_of_task),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (Common.ifAlmuerzo == 1) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_schedule_lunch),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (Common.ifBreack == 1) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_schedule_breack),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (Common.ifPermiso == 1) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.check_schedule_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        imgCerrar.setOnClickListener {
            try {
                showTarea?.dismiss()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        btnStandBy.setOnClickListener {
            showTarea?.dismiss()
            val inflaterpop =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            popupstandby(inflaterpop)
        }
        rechargeLists()
        return false
    }

    private fun popupstandby(inflater: LayoutInflater) {
        val fg = FuncionesGenerales()
        val lts = LoadTaskSqlite()
        val sinco = SincronizarOnline()
        sinco.sincronizarTablasVF(requireContext())
        val bk = Backups()
        bk.backupdDatabase(requireContext())
        val popUp = inflater.inflate(R.layout.stanby_layout, null)
        val titulo = popUp.findViewById<View>(R.id.tvTitulo) as TextView
        val motivo = popUp.findViewById<View>(R.id.etObservation) as EditText
        titulo.setText("Ingrese el motivo para detener la ejecución de la tarea")
        val cancel = popUp.findViewById<View>(R.id.btnCancelarSB) as TextView
        val ok = popUp.findViewById<View>(R.id.btnIngresarST) as TextView
        val popupWindow = PopupWindow(popUp, LinearLayout.LayoutParams.MATCH_PARENT, 500, true)
        val latitud_ult = fg?.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg?.parametro(requireContext(), "ULT_LON")
        var latact = latitud_ult
        var lonact = longitud_ult
        cancel.setOnClickListener { popupWindow.dismiss() }
        ok.setOnClickListener {
            if (motivo.text.toString().length > 3) {
                try{
                    fg.act_estadotareaandsync(requireContext(),idtarea,"Pendiente","0","1")
                }finally {
                    try {
                        fg.ins_historial(requireContext(), motivo.text.toString(), latact, lonact)
                    } finally {
                        try {
                            lts.historynotes(requireContext(), motivo.text.toString())
                        } finally {
                            findNavController().navigate(R.id.navigation_tasks)
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

    private fun rechargeLists() {

        if (listRutes.visibility == View.VISIBLE || listRutesFin.visibility == View.VISIBLE) {
            view?.let { cargarRuta(it) }
        }
    }

    private fun showViewAndButton(button: Button) {
        button.visibility = View.VISIBLE
    }

    private fun hideViewAndButton(button: Button) {
        button.visibility = View.GONE
    }

    private fun openFragmentWithLocation(latLng: LatLng) {
        val bundle = Bundle()
        bundle.putDouble("latitude", latLng.latitude)
        bundle.putDouble("longitude", latLng.longitude)
        findNavController().navigate(R.id.navigation_map, bundle)
    }

    private fun openFragmentForShowRute(lat: String, long: String) {
        val bundle = Bundle()
        bundle.putString("latitudeS", lat)
        bundle.putString("longitudeS", long)
        findNavController().navigate(R.id.navigation_map, bundle)
    }

    private fun openFragment() {
        findNavController().navigate(R.id.navigation_map)
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
        var coditems: Array<String?>
        var spintxtitems: Array<String?>
        val popUp = inflater.inflate(R.layout.valexitoso_layout, null)
        val pregunta = popUp.findViewById<View>(R.id.tvPregunta) as TextView
        val etObserv = popUp.findViewById<View>(R.id.et_ObservNoExit) as EditText
        val spinnoexit = popUp.findViewById<View>(R.id.spinnerNoExitoso) as Spinner
        val popupWindow = PopupWindow(popUp, LinearLayout.LayoutParams.MATCH_PARENT, 500, true)
        val finalizar = popUp.findViewById<View>(R.id.btnFinalizar) as TextView
        var posicion = "0"
        val querySpin =
            "SELECT CAST(idnoexitoso as INTEGER) as idne,noexitoso FROM '105_NO_EXITOSO' order by 1 asc"
        val objSpin =
            conGen.queryObjeto2val(requireContext(), querySpin, null)
        if (objSpin.isNotEmpty()) {
            spincoditems = arrayOfNulls(objSpin.size + 1)
            spintxtitems = arrayOfNulls(objSpin.size + 1)
            spincoditems[canitems] = "0"
            spintxtitems[canitems] = "Seleccione una opción"
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
            spinnoexit.onItemSelectedListener = object : OnItemSelectedListener {
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
                    findNavController().navigate(R.id.navigation_tasks)
                    popupWindow.dismiss()
                } else {
                    Toast.makeText(requireContext(),
                        "Debe Seleccionar un motivo  y escribir una observación para continuar",
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
            fg.act_estadotarea(requireContext())
            fg.ins_historial(requireContext(), "0", latact, lonact)
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


    fun upLoadImage() {
        val waitingDialog = SpotsDialog.Builder().setContext(requireContext()).build()
        waitingDialog.show()
        val arrayStrImgPath = Paper.book().read<java.util.ArrayList<String>>("listImage")
        val arrayMapEmail =
            Paper.book().read<java.util.ArrayList<MutableMap<String, String>>>("listEmail")
        var cantUpEmail = 0
        try {
            val cantEmail = arrayMapEmail.size
            if (cantEmail != 0) {
                waitingDialog.setMessage("Cargando: ${(cantUpEmail + 1)} de $cantEmail")
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.not_images_upload),
                    Toast.LENGTH_SHORT
                ).show()
                waitingDialog.dismiss()
            }
            for (params: MutableMap<String, String> in arrayMapEmail) {
                val queue = Volley.newRequestQueue(requireContext())
                // Request a string response from the provided URL.
                val stringRequest: StringRequest =
                    object : StringRequest(Method.POST, Common.urlApi,
                        Response.Listener {
                            android.util.Log.e("tcgoRute", "3")
                        }, Response.ErrorListener {
                        }) {
                        override fun getParams(): MutableMap<String, String> {
                            return params
                        }
                    }

                // Add the request to the RequestQueue.
                queue.add(stringRequest)
                cantUpEmail++
                waitingDialog.setMessage("Cargando: " + (cantUpEmail + 1) + " de " + cantEmail)
                arrayMapEmail.remove(params)
                Paper.book().write("listEmail", arrayStrImgPath)
                if (cantEmail == cantUpEmail) {
                    waitingDialog.dismiss()
                }
            }
        } catch (e: java.lang.Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.not_images_upload),
                Toast.LENGTH_SHORT
            ).show()
            waitingDialog.dismiss()
        }
        var cantUpLoad = 0
        try {
            val cantImg = arrayStrImgPath.size
            if (cantImg != 0) {
                waitingDialog.setMessage("Cargando: ${(cantUpLoad + 1)} de $cantImg")
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.not_images_upload),
                    Toast.LENGTH_SHORT
                ).show()
                waitingDialog.dismiss()
            }
            for (
            strImgPath: String in arrayStrImgPath) {
                android.util.Log.e("tcgoUpLoad", strImgPath)
                val file = java.io.File(strImgPath)
                var bitmap = BitmapFactory.decodeFile(strImgPath)
                bitmap = Tools.resizeBitmap(bitmap, 700)
                val baos = java.io.ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val mountainsRef = storageRef.child(file.name)
                val uploadTask = mountainsRef.putBytes(data)
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    cantUpLoad++
                    waitingDialog.setMessage("Cargando: " + (cantUpLoad + 1) + " de " + cantImg)
                    arrayStrImgPath.remove(strImgPath)
                    Paper.book().write("listImage", arrayStrImgPath)
                    android.util.Log.e("tcgoUpLoad", "UpLoadImage" + taskSnapshot.uploadSessionUri)
                    if (cantImg == cantUpLoad) {
                        waitingDialog.dismiss()
                    }
                }.addOnFailureListener { android.util.Log.e("tcgoUpLoad", "No UpLoadImage") }
            }
        } catch (e: java.lang.Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.not_images_upload),
                Toast.LENGTH_SHORT
            ).show()
            waitingDialog.dismiss()
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
        fg.act_param(requireContext(), "ESTADOREC_ACT", intEstado.toString())
        val latitud_ult = fg.parametro(requireContext(), "ULT_LAT")
        val longitud_ult = fg.parametro(requireContext(), "ULT_LON")
        val latact = latitud_ult
        val lonact = longitud_ult
        fg.ins_historial(requireContext(), "0", latact, lonact)
        fg.act_estadotarea(requireContext())
        updateInfoRed = HashMap()
        val updateInfo2: MutableMap<String, Any> = HashMap()
        val updateInfo3: MutableMap<String, Any> = HashMap()
        val horaNow = Date()
        var boolRecKmTiempo = true
        updateInfoRed = Common.mapRegistroUs!!["Recorrido"] as MutableMap<String, Any>?
        val mapLatLng = arrayService[zIndex]["latLng"] as Map<String, Any>
        riderLatDest = mapLatLng["latitude"].toString()
        riderLngDest = mapLatLng["longitude"].toString()
        val estadorec = fg.estadorec_act(requireContext())
        if (estadorec.toString() == "0") {
            updateInfoRed?.put("RecKmReal", Paper.book().read<Any>("mtRec"))
            updateInfoRed?.put("RecTiempoReal", "00:00")
        } else if ((Common.mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["Estado"].toString() == "1") {
            if (!(Common.mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["RecHoraIn"].toString()
                    .isEmpty()
            ) {
                try {
                    val horaIn = Date()
                    val recTiempo = Date()
                    horaIn.hours =
                        Common.formatHora.parse((Common.mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["RecHoraIn"].toString()).hours
                    horaIn.minutes =
                        Common.formatHora.parse((Common.mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["RecHoraIn"].toString()).minutes
                    recTiempo.hours = horaNow.hours - horaIn.hours
                    recTiempo.minutes = horaNow.minutes - horaIn.minutes
                    updateInfoRed?.put("RecTiempoReal", Common.formatHora.format(recTiempo))
                    updateInfoRed?.put("RecKmReal", Paper.book().read<Any>("mtRec").toString() + "")
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        } else {
            updateInfoRed?.put("RecHoraIn2", Common.formatHora.format(horaNow))
            val estadorec = fg.estadorec_act(requireContext())
            if (estadorec.toString() != "5" &&
                estadorec.toString() != "2" &&
                estadorec.toString() != "3"
            ) {
                try {
                    val horaIn = Date()
                    val recTiempo = Date()
                    val tiempoReal = Date()
                    horaIn.hours =
                        Common.formatHora.parse((Common.mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["RecHoraIn2"].toString()).hours
                    horaIn.minutes =
                        Common.formatHora.parse((Common.mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["RecHoraIn2"].toString()).minutes
                    tiempoReal.hours =
                        Common.formatHora.parse((Common.mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["RecTiempoReal"].toString()).hours
                    tiempoReal.minutes =
                        Common.formatHora.parse((Common.mapRegistroUs!!["Recorrido"] as Map<String?, Any>?)!!["RecTiempoReal"].toString()).minutes
                    recTiempo.hours = horaNow.hours - horaIn.hours + tiempoReal.hours
                    recTiempo.minutes = horaNow.minutes - horaIn.minutes + tiempoReal.minutes
                    updateInfoRed?.put("RecTiempoReal", Common.formatHora.format(recTiempo))
                    updateInfoRed?.put("RecKmReal", Paper.book().read<Any>("mtRec").toString() + "")
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        }
        updateInfoRed?.put("Estado", intEstado.toString() + "")
        if (intEstado != 0) {
            updateInfoRed?.put("IdTarea", arrayService[zIndex]["key"].toString())
        }
        val mapRed: MutableMap<String, Any> = HashMap()
        when (intEstado) {
            0 -> {
                updateInfoRed?.put("IdTarea", "")
                updateInfoRed?.put("RecHoraFin", "")
                updateInfoRed?.put("RecHoraIn", "")
                updateInfoRed?.put("RecHoraIn2", "")
                updateInfoRed?.put("RecKmEst", "")
                updateInfoRed?.put("RecKmReal", "")
                updateInfoRed?.put("RecTiempoEst", "")
                updateInfoRed?.put("RecTiempoReal", "")
                Common.boolRecKm = false
                boolRecKmTiempo = false
                Paper.book().write("mtRec", 0.0)
                mapRed["Recorrido"] = updateInfoRed as TypeVariable<*>
                Common.dbRegistroUs?.document(Common.mapRegistroUs!!["key"].toString())
                    ?.update(mapRed)
            }
            1 -> {
                Common.boolRecKm = true
                if (estadorecant.toString() == "0") {
                    updateInfoRed?.put(
                        "ubicacion",
                        LatLng(latact.toDouble(), lonact.toDouble())
                    )
                    updateInfoRed?.put("RecHoraIn", Common.formatHora.format(horaNow))
                }
                Common.recIdTarea = arrayService[zIndex]["key"].toString()
                try{
                    fg.act_estadotareapro(requireContext(), idtarea, "En Recorrido")
                }finally {
                    if (estadorecant.toString() != "1") {
                    riderLatDest?.let { it1 ->
                        riderLngDest?.let { it2 ->
                            openFragmentForShowRute(
                                it1,
                                it2
                            )
                        }
                    }
                    }
                }
            }
            2 -> {
                updateInfo2["estado"] = "En Proceso"
                val mapEje = arrayService[zIndex]["ejecucion"] as MutableMap<String?, Any?>
                mapEje["abierta"] = "1"
                updateInfo2["ejecucion"] = mapEje
                Common.dbServices!!.document(arrayService[zIndex]["key"].toString())
                    .update(updateInfo2)
                Common.recIdTarea = arrayService[zIndex]["key"].toString()
                Common.boolRecKm = false
                updateInfoRed?.put("RecHoraIn2", Common.formatHora.format(horaNow))
                boolRecKmTiempo = false
                mapRed["Recorrido"] = updateInfoRed as MutableMap<*, *>
                Common.dbRegistroUs!!.document(Common.mapRegistroUs!!["key"].toString())
                    .update(mapRed)
                fg.act_estadotareapro(requireContext(), idtarea, "En Proceso")
            }
            3 -> {
                Common.recIdTarea = arrayService[zIndex]["key"].toString()
                val nota =
                    """ Hora: ${Common.formatHora.format(Date())} Usuario: ${Common.documentUser!!["nombres"].toString()} ${Common.documentUser!!["apellidos"].toString()}Nota: Se reporta tiempo muerto"""
                val mapService = arrayService[zIndex]
                val lts = LoadTaskSqlite()
                lts.historynotes(requireContext(), nota);
                Common.boolRecKm = false
                boolRecKmTiempo = false
                /*updateInfoRed?.put("RecHoraIn2", Common.formatHora.format(horaNow))
                mapRed["Recorrido"] = updateInfoRed as TypeVariable<*>
                Common.dbRegistroUs!!.document(Common.mapRegistroUs!!["key"].toString())
                    .update(mapRed)*/
            }
            4 -> {
                Common.boolRecKm = true
                updateInfoRed?.put("RecHoraIn2", Common.formatHora.format(horaNow))
                Common.recIdTarea = arrayService[zIndex]["key"].toString()
                try{
                    fg.act_estadotareac(requireContext(), idtarea, "En Recorrido")
                }finally {
                    riderLatDest?.let { it1 ->
                        riderLngDest?.let { it2 ->
                            openFragmentForShowRute(
                                it1,
                                it2
                            )
                        }
                    }
                }
            }
            5 -> {
                try{
                    fg.act_recorridosapend(requireContext())
                }finally {
                    Common.boolRecKm = false
                    updateInfoRed?.put("IdTarea", "")
                    updateInfoRed?.put("RecHoraIn2", Common.formatHora.format(horaNow))
                    updateInfoRed?.put("RecKmEst", "")
                    updateInfoRed?.put("RecTiempoEst", "")
                    boolRecKmTiempo = false
                    mapRed["Recorrido"] = updateInfoRed as MutableMap<String, Any>
                    Common.dbRegistroUs!!.document(Common.mapRegistroUs!!["key"].toString())
                        .update(mapRed)
                    findNavController().navigate(R.id.navigation_tasks)
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = TasksFragment()
    }
}