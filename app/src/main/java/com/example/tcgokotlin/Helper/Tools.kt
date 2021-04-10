package com.example.tcgokotlin.Helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tcgokotlin.R
import com.example.tcgokotlin.utils.AnimationUtils.NotificationAlerter
import com.example.tcgokotlin.utils.Common
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLDecoder
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Tools {

    companion object {
        fun convertObjToMapReflection(obj: Any): Map<String, Any> {
            val mapObjects: MutableMap<String, Any> = HashMap()
            val allFields = obj.javaClass.declaredFields
            for (field in allFields) {
                field.isAccessible = true
                var value: Any = ""
                try {
                    value = field[obj]
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
                mapObjects[field.name] = value
            }
            return mapObjects
        }

        fun showView(view: View) {
            view.visibility = View.VISIBLE
        }

        fun hideView(view:View) {
            view.visibility = View.GONE
        }

        fun dispatchTakePictureIntent(fragment: Fragment) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            fragment.startActivityForResult(takePictureIntent, Common.REQUEST_IMAGE_CAPTURE)
        }

        fun createMutableMap(position: Int, array: ArrayList<MutableMap<String, Any>>): MutableMap<String, Any> {
            return array[position]["latLng"] as MutableMap<String, Any>
        }

        fun checkPermissionsUbication(context: Context): Boolean {
            if (ActivityCompat.checkSelfPermission( context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return true
        }

        fun decodePoly(encoded: String): List<LatLng>? {
            val poly: MutableList<LatLng> = java.util.ArrayList()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0
            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat
                shift = 0
                result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng
                val p = LatLng(lat.toDouble() / 1E5,
                        lng.toDouble() / 1E5)
                poly.add(p)
            }
            return poly
        }

        fun returnCurrentTime(): String {
            val pattern = "yyyy-MM-dd"
            val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            val currentTime = Calendar.getInstance().time
            return simpleDateFormat.format(currentTime)
        }

        fun getCurrentHour(): String {
            val pattern = "HH:ss"
            val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            val currentTime = Calendar.getInstance().time
            return simpleDateFormat.format(currentTime)
        }

        fun convertStringInDate(stringDate: String): Date? {
            val pattern = "HH:ss"
            val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            return simpleDateFormat.parse(stringDate)
        }

        fun resizeBitmap(getBitmap: Bitmap, maxSize: Int): Bitmap? {
            return try {
                val width = getBitmap.width
                var height = getBitmap.height
                val porcentaje: Double
                porcentaje = height * 100 / width.toDouble()
                height = (maxSize * porcentaje).toInt() / 100
                Bitmap.createScaledBitmap(getBitmap, maxSize, height, false)
            } catch (e: Exception) {
                null
            }
        }

        fun uploadImageUser(image:File, iv: CircleImageView, context: Context, activity: Activity) {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child("users")
            val file = File(image.path)
            val uri = Uri.fromFile(file)
            var bitmap = BitmapFactory.decodeFile(image.path)
            bitmap = resizeBitmap(bitmap, 700)
            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val nameRef = storageRef.child(file.name)
            val uploadTask = nameRef.putFile(uri)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                saveImgUser(image.name, iv, context)
            }.addOnFailureListener {
                NotificationAlerter.createAlertError(context.getString(R.string.problems_upload_images), activity)
            }
        }

        fun uploadImageProfile(image:File, iv: ImageView, context: Context, activity: Activity, namePhoto: String, view:View, isString: Boolean) {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child("profile")
            val file = File(image.path)
            val uri = Uri.fromFile(file)
            var bitmap = BitmapFactory.decodeFile(image.path)
            bitmap = resizeBitmap(bitmap, 700)
            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val nameRef = storageRef.child(file.name)
            val uploadTask = nameRef.putFile(uri)
            showView(view)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                saveImgProfile(image.name, iv, namePhoto, context, activity, view, isString)
            }.addOnFailureListener {
                hideView(view)
                NotificationAlerter.createAlertError(context.getString(R.string.problems_upload_images), activity)
            }
        }

        fun createImageFile(context: Context): File {
            // Create an image file name
            val formatDateHoraSeg2 = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault())
            val timeStamp = formatDateHoraSeg2.format(Date())
            val imageFileName = "TCGO_${timeStamp}_"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            // Save a file: path for use with ACTION_VIEW intents
            return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir /* directory */
            )
        }

        fun saveImgUser(name:String, iv: CircleImageView, context: Context) {
            val mapImg: MutableMap<String, Any> = HashMap()
            val mapLatLng: MutableMap<String, Any> = HashMap()
            mapLatLng["latitude"] = Common.mLastLocation?.latitude ?: 0.0
            mapLatLng["longitude"] = Common.mLastLocation?.longitude ?: 0.0
            mapImg["hora"] = Common.formatHora.format(Date())
            mapImg["latLng"] = mapLatLng
            mapImg["url"] = "https://firebasestorage.googleapis.com/v0/b/tggodes.appspot.com/o/users%2F${name}?alt=media"
            Common.documentUser?.set("fotoConductor", mapImg)
            Common.documentUser?.let {
                Common.dbDriversInformation?.document(it["key"].toString())?.update(it)
            }
            Glide.with(context).load("${mapImg["url"]}").placeholder(R.drawable.ic_home_place_holder_profile).error(R.drawable.ic_home_place_holder_profile).into(iv)
        }

        fun saveImgProfile(name:String, iv: ImageView, namePhoto: String, context: Context, activity: Activity, view: View, isString: Boolean) {
            if (isString) {
                val fotoResibo = "https://firebasestorage.googleapis.com/v0/b/tggodes.appspot.com/o/profile%2F${name}?alt=media"
                (Common.documentUser?.get("images") as MutableMap<String,Any>).set(namePhoto, fotoResibo)
            } else {
                val mapImg: MutableMap<String, Any> = HashMap()
                mapImg["url"] = "https://firebasestorage.googleapis.com/v0/b/tggodes.appspot.com/o/profile%2F${name}?alt=media"
                (Common.documentUser?.get("images") as MutableMap<String,Any>).set(namePhoto, mapImg)
            }
            Common.documentUser?.let {
                Common.dbDriversInformation?.document(it["key"].toString())?.update(it)?.addOnSuccessListener {
                    hideView(view)
                    NotificationAlerter.createAlert(context.getString(R.string.your_image_has_charge_successfull), activity)
                    iv.setImageResource(R.drawable.ic_camera_alt_blue_24dp)
                }?.addOnFailureListener {
                    hideView(view)
                    NotificationAlerter.createAlertError(context.getString(R.string.problems_upload_images), activity)
                }
            }
        }

        fun getRecEstado(): Int {
            var recEstado = 0
            val recorrido = Common.mapRegistroUs?.get("Recorrido") as MutableMap<String?, Any>?
            recEstado = recorrido?.get("Estado").toString().toInt()
            return recEstado
        }

        fun getMetros(mLatitude: Double, mLongitude: Double): Double{
            return calcCrow(Common.mLastLocation?.latitude ?: 0.0, Common.mLastLocation?.longitude
                    ?: 0.0, mLatitude, mLongitude)
        }

        fun calcCrow(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val R = 6371.0 // km
            val dLat = toRad(lat2 - lat1)
            val dLon = toRad(lon2 - lon1)
            val mlat1 = toRad(lat1)
            val mlat2 = toRad(lat2)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(mlat1) * Math.cos(mlat2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return R * c
        }

        private fun toRad(Value: Double): Double = Value * Math.PI / 180

        fun chargeArrayService() : ArrayList<MutableMap<String, Any>> {
            val arrayService: ArrayList<MutableMap<String, Any>> = ArrayList()
            try {
                for (  mapService: MutableMap<String, Any>? in Common.arrayServicePen!!) {
                    arrayService.add(mapService!!)
                }
            }catch (e: java.lang.Exception){}
            try {
                for (  mapService: MutableMap<String, Any>? in Common.arrayServicePen1!!) {
                    arrayService.add(mapService!!)
                }
            }catch (e: java.lang.Exception){}
            try {
                for (  mapService: MutableMap<String, Any>? in Common.arrayServiceRec!!) {
                    arrayService.add(mapService!!)
                }
            }catch (e: java.lang.Exception){}
            try {
                for (  mapService: MutableMap<String, Any>? in Common.arrayServiceRec1!!) {
                    arrayService.add(mapService!!)
                }
            }catch (e: java.lang.Exception){}
            try {
                for (  mapService: MutableMap<String, Any>? in Common.arrayServicePro!!) {
                    arrayService.add(mapService!!)
                }
            }catch (e: java.lang.Exception){}
            try {
                for (  mapService: MutableMap<String, Any>? in Common.arrayServicePro1!!) {
                    arrayService.add(mapService!!)
                }
            }catch (e: java.lang.Exception){}
            return arrayService
        }

        fun chargeArrayServiceFinish(): ArrayList<MutableMap<String, Any>> {
            val arrayServiceFinish: ArrayList<MutableMap<String, Any>> = ArrayList()
            try {
                for (  mapService: MutableMap<String, Any>? in Common.arrayServiceFin!!) {
                    arrayServiceFinish.add(mapService!!)
                }
            }catch (e: java.lang.Exception){}
            return arrayServiceFinish
        }


        fun GetTicks(): String {
            val TICKS_AT_EPOCH = 621355968000000000L
            val tick = System.currentTimeMillis() * 10000 + TICKS_AT_EPOCH
            return tick.toString()
        }

        fun GetDate(): String {
            val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
            val date = Date()
            return dateFormat.format(date)
        }

        fun GetTime(): String {
            val dateFormat: DateFormat = SimpleDateFormat("hhmmss")
            val date = Date()
            return dateFormat.format(date)
        }


        fun compressImage(myBitmap: Bitmap, strPath: String?): Bitmap {
            var myBitmap = myBitmap
            var iWith = myBitmap.width
            var iHeight = myBitmap.height
            while (iWith > 600 && iHeight > 600) {
                iWith = iWith / 2
                iHeight = iHeight / 2
            }
            myBitmap = Bitmap.createScaledBitmap(myBitmap, iWith, iHeight, false)
            try {
                FileOutputStream(strPath).use { out ->
                    myBitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            90,
                            out
                    ) // bmp is your Bitmap instance
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // m//yBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            return myBitmap
        }

       /* fun fillSpinner(spn: Spinner, _list: List<String?>, ctx: Context) {
            _list.add(0, ctx.getString(R.string.select))
            val adapter = ArrayAdapter(
                ctx, R.layout.ly_item_arrow_spinner, _list
            )
            adapter.setDropDownViewResource(R.layout.ly_item_spinner)
            spn.adapter = adapter
        }

        fun fillSpinnerNormal(spn: Spinner, _list: List<String>?, ctx: Context?) {
            val adapter = ArrayAdapter(
                ctx!!, R.layout.ly_item_arrow_spinner, _list
            )
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            spn.adapter = adapter
        }*/

        fun getMonthNameByNumber(iMonth: Int): String {
            var iMonth = iMonth
            iMonth = iMonth - 1
            var strMonth = ""
            val dfs = DateFormatSymbols(Locale("es"))
            val months = dfs.months
            if (iMonth >= 0 && iMonth <= 11) {
                strMonth = months[iMonth]
            }
            return strMonth
        }

        fun setSpinner(spn: Spinner, strValue: String?) {
            spn.setSelection((spn.adapter as ArrayAdapter<String?>).getPosition(strValue))
        }

        fun drawTextToBitmap(mContext: Context, bitmap: Bitmap, mText: String): Bitmap? {
            var bitmap = bitmap
            return try {
                val resources = mContext.resources
                val scale = resources.displayMetrics.density
                var bitmapConfig = bitmap.config
                // set default bitmap config if none
                if (bitmapConfig == null) {
                    bitmapConfig = Bitmap.Config.ARGB_8888
                }
                // resource bitmaps are imutable,
                // so we need to convert it to mutable one
                bitmap = bitmap.copy(bitmapConfig, true)
                val canvas = Canvas(bitmap)
                // new antialised Paint
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                // text color - #3D3D3D
                paint.color = Color.WHITE
                // text size in pixels
                //paint.setTextSize((int) (14 * scale));
                paint.textSize = 40f
                val size = 0f
                do {
                    paint.textSize = size + 1
                } while (paint.measureText(mText) < bitmap.width - bitmap.width / 2 / 2)


                // text shadow
                paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY)

                // draw text to the Canvas center
                val bounds = Rect()
                paint.getTextBounds(mText, 0, mText.length, bounds)
                val x = bitmap.width - bounds.width()
                val y = bitmap.height - bounds.height()
                canvas.drawText(mText, 40f, y.toFloat(), paint)
                bitmap
            } catch (e: Exception) {
                null
            }
        }

        fun Scape(strData: String?) {}
        fun UnScape(strData: String): String {
            return try {
                URLDecoder.decode(strData, "UTF-8")
            } catch (e: Exception) {
                strData
            }
        }

        fun CapitalLetter(strData: String): String {
            return strData.substring(0, 1).toUpperCase() + strData.substring(1)
        }

     /*   fun createAlert(ctx: Context?, strMessage: String?) {
            val mDialogView: View = LayoutInflater.from(ctx).inflate(R.layout.dl_alert, null)
            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(
                ctx!!
            )
                .setView(mDialogView)
            //show dialog
            val mAlertDialog = mBuilder.create()
            mAlertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mAlertDialog.show()
            (mAlertDialog.findViewById<View>(R.id.tvMessageAlert) as TextView?)!!.text =
                strMessage
            (mAlertDialog.findViewById<View>(R.id.imgClose) as ImageView?)!!.setOnClickListener { mAlertDialog.dismiss() }
        }
*/
        fun getMoth(month: String): Int {
            var date: Date? = null
            try {
                date = SimpleDateFormat("MMMM", Locale("es")).parse(month.toLowerCase())
            } catch (ignored: ParseException) {
            }
            val cal = Calendar.getInstance()
            cal.time = date
            return cal[Calendar.MONTH]
        }

        val currentDate: Date
            get() = Calendar.getInstance().time

        fun hideKeyboard(activity: Activity) {
            // Check if no view has focus:
            val view = activity.currentFocus
            if (view != null) {
                val inputManager =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(
                        view.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                )
                inputManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
            }
        }

        fun rotateImageIfRequired(context: Context?, img: Bitmap, path: String?): Bitmap {
            var ei: ExifInterface? = null
            try {
                ei = ExifInterface(path!!)
            } catch (e: IOException) {
            }
            val orientation = ei!!.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
            )
            var rotatedBitmap = img
            rotatedBitmap =
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
                    ExifInterface.ORIENTATION_NORMAL -> img
                    else -> img
                }
            return rotatedBitmap
        }

        fun getUriFromPath(context: Context?, destination: String?): Uri {
            val file = File(destination)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context!!, "com.app.diaco.fileprovider", file)
            } else {
                Uri.fromFile(file)
            }
        }

        fun rotateImage(source: Bitmap, angle: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(
                    source, 0, 0, source.width, source.height,
                    matrix, true
            )
        }

        /**
         * Get the rotation of the last image added.
         *
         * @param context
         * @param selectedImage
         * @return
         */
        private fun getRotation(context: Context, selectedImage: Uri): Int {
            var rotation = 0
            val content = context.contentResolver
            val mediaCursor = content.query(
                    selectedImage, arrayOf("orientation", "date_added"),
                    null, null, "date_added desc"
            )
            if (mediaCursor != null && mediaCursor.count != 0) {
                while (mediaCursor.moveToNext()) {
                    if (mediaCursor.columnCount > 0) {
                        rotation = mediaCursor.getInt(0)
                    }
                    break
                }
            }
            mediaCursor!!.close()
            return rotation
        }
    }
}