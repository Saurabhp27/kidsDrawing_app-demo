package com.example.kidsdrawingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    val openGalleryLauncher :ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if (result.resultCode == RESULT_OK && result.data != null){
                val imagebackground : ImageView = findViewById(R.id.backgroundIMage)
                imagebackground.setImageURI(result.data?.data)
            }
        }

    val requestpermission : ActivityResultLauncher<Array<String>>  =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            Permissions ->
            Permissions.entries.forEach{
                val permisssionName = it.key
                val isGranted = it.value

                if(isGranted){
                    Toast.makeText(this,"permission granted",Toast.LENGTH_SHORT).show()

                    val pickintent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickintent)
                }
                else{
                    if (permisssionName == Manifest.permission.READ_EXTERNAL_STORAGE){
                        Toast.makeText(this,"not granted for reading external files", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    private fun isReadStorageAllowed(): Boolean {

        val result = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private var drawingView : DrawingView? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setsizeofbrush(20.toFloat())

        val ib_brush : ImageButton = findViewById(R.id.ib_brush)
        ib_brush.setOnClickListener(){
            showBrushSizechooserDailogue()
        }

        val ib_gallery : ImageButton = findViewById(R.id.gallery)
        ib_gallery.setOnClickListener(){
            requeststorageperion()
        }

        val ib_save : ImageButton = findViewById(R.id.save_btn)
        ib_save.setOnClickListener() {
            val fl_view: FrameLayout = findViewById(R.id.fl_drawing_view_container)
            if (isReadStorageAllowed()) {
                lifecycleScope.launch {
                    saveBitmapFile(getBitmapFromView(fl_view))
                }
            }
        }



    }




    private fun shareImage(result: String){
        MediaScannerConnection.scanFile(this, arrayOf(result), null ){
            path, uri ->
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "Image/png"
            startActivity(Intent.createChooser(shareIntent, "Share"))

        }
    }

    private fun requeststorageperion(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            ShowRationalDilog("Kids Drawing App", "It needs to accesss the External storage")
        }
        else
        {
            requestpermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }

    private fun ShowRationalDilog(title : String, msg : String){
        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(msg).setPositiveButton("Cancel"){dialog , _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun showBrushSizechooserDailogue (){
        val brushdailogue = Dialog(this)
        brushdailogue.setContentView(R.layout.dailogue_brush_size)
        val smallbtn : ImageButton = brushdailogue.findViewById(R.id.Brush_size_small)
        smallbtn.setOnClickListener(){
            drawingView?.setsizeofbrush(10.toFloat())
            brushdailogue.dismiss()
        }

        val mediumbtn : ImageButton = brushdailogue.findViewById(R.id.ib_medium_brush)
        mediumbtn.setOnClickListener(){
            drawingView?.setsizeofbrush(20.toFloat())
            brushdailogue.dismiss()
        }
        val largebtn : ImageButton = brushdailogue.findViewById(R.id.ib_large_brush)
        largebtn.setOnClickListener(){
            drawingView?.setsizeofbrush(30.toFloat())
            brushdailogue.dismiss()
        }
        brushdailogue.show()

    }

    private fun getBitmapFromView(view: View): Bitmap {

        //Define a bitmap with the same size as the view.
        // CreateBitmap : Returns a mutable bitmap with the specified width and height
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

//    private suspend fun savebitmapfile (mBitmap : Bitmap) : String{
//        var result = ""
//        withContext(Dispatchers.IO)
//    }

    private suspend fun saveBitmapFile(mBitmap: Bitmap?):String{
        var result = ""
        withContext(Dispatchers.IO) {
            if (mBitmap != null) {

                try {
                    val bytes = ByteArrayOutputStream() // Creates a new byte array output stream.
                    // The buffer capacity is initially 32 bytes, though its size increases if necessary.

                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    val f = File(
                        externalCacheDir?.absoluteFile.toString()
                                + File.separator + "KidDrawingApp_" + System.currentTimeMillis() / 1000 + ".jpg"
                    )

                    val fo = FileOutputStream(f) // Creates a file output stream to write to the file represented by the specified object.
                    fo.write(bytes.toByteArray()) // Writes bytes from the specified byte array to this file output stream.
                    fo.close() // Closes this file output stream and releases any system resources associated with this stream. This file output stream may no longer be used for writing bytes.
                    result = f.absolutePath // The file absolute path is return as a result.
                    //We switch from io to ui thread to show a toast
                    runOnUiThread {
                        if (!result.isEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "File saved successfully :$result",
                                Toast.LENGTH_SHORT
                            ).show()
                            shareImage(result)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Something went wrong while saving the file.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }
}