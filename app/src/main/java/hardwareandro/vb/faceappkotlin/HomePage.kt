package hardwareandro.vb.faceappkotlin

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import hardwareandro.vb.faceappkotlin.Model.Data
import hardwareandro.vb.faceappkotlin.Model.PostInfo
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_post.view.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class HomePage : AppCompatActivity() {

    var FacePostList = ArrayList<Data>()
    var adapter:MyFaceAdapter?=null
    var myEmail:String?=null
    var UserUID:String?=null

    private  var database= FirebaseDatabase.getInstance()
    private  var myRef = database.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        var b:Bundle = intent.extras
        myEmail=b.getString("email")
        UserUID=b.getString("uid")



        FacePostList.add(Data("1234","Hello HardwareAndro","cpu","add"))
        FacePostList.add(Data("12345","Hello HardwareAndro2","cpu","veli"))
        FacePostList.add(Data("12345","Hello HardwareAndro2","cpu","veli2"))
        FacePostList.add(Data("12345","Hello HardwareAndro2","cpu","veli3"))

        adapter=MyFaceAdapter(this,FacePostList)
        _lstFace.adapter=adapter
    }

    inner class MyFaceAdapter: BaseAdapter {

        //geçici bir liste oluşturuyoruz gelen listeyi karşılamak için
        var listpostAdapter =ArrayList<Data>()
        //gelen page karşılamak için
        var context: Context?= null



        constructor(context: Context,listpostAdapter:ArrayList<Data>):super()
        {
            //gerekli atama işlemleri
            this.listpostAdapter=listpostAdapter
            this.context=context

        }


        //listemizin döndüren fonksiyon
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            //burada listede kaç tane elaman var postion  o kadar tekrar eder
            //gelen listedeki itemleri geçici bir değişkende tutuyoruz
            var myPost = listpostAdapter[position]

            if (myPost.postPersonUID.equals("add")){

                //eğer gelen itemin  UID değeri add ise add_post itemini döndüryoruz
                var myView= layoutInflater.inflate(R.layout.add_post,null)

                myView.imgFileAttach.setOnClickListener {

                    loadImage()
                }

                myView.imgSend.setOnClickListener{
                    var data:PostInfo = PostInfo(UserUID,myView.etUserThink.text.toString(),DowloadURL);
                    myRef.child("posts").push().setValue(data)

                    myView.etUserThink.setText("")
                  //  myRef.child("posts").push().child("UserUID").setValue(UserUID)
                  //  myRef.child("posts").push().child("text").setValue(myView.etUserThink.text.toString())
                  //  myRef.child("posts").push().child("postImage").setValue(DowloadURL)
                }
                return myView
            }
            else{
                //yok değil ise normal bir post ise user post itemini döndürüyoruz
                var myView= layoutInflater.inflate(R.layout.face_post,null)
                return myView
            }

        }

        // listedeki ıtemlerin id ve count gibi durumlarını bize döndüren
        //fonksiyonlar
        override fun getItem(position: Int): Any {
            return listpostAdapter[position]
        }

        override fun getItemId(position: Int): Long {
            return  position.toLong()
        }

        override fun getCount(): Int {
            return  listpostAdapter.count()
        }


    }

    //upload image
    //burada random bir değer oluşturuyoruz doğru ise arkada yakalamak için
    var PICK_IMAGE_CODE:Int = 15
    private fun loadImage() {

        //bir istek atıyoruz ve cihazdan fotoğraf okumak için
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //intent den gelen değer ve içinde birşey olup olmamasını kontrol ediyoruz
        if (requestCode == PICK_IMAGE_CODE && data != null) {

            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)
            cursor.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()
            UploadImage( BitmapFactory.decodeFile(picturePath))

        }
    }
    var bitmapx:Bitmap?=null

    var DowloadURL:String?=null
    private fun UploadImage(bitmap: Bitmap?) {

        //storage bağlantı sağlıyoruz
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://faceappskotlin.appspot.com")

        //date yi alarak profil resminde bu stringi kullanacağız
        val df = SimpleDateFormat("ddMMyyHHmmss")
        val dataObj= Date()

        //myemail sisteme kayıt olan kişinin numaası
        val imagePath=SplitString(myEmail!!) + "." + df.format(dataObj)+".jpg"

        val ImageRef = storageRef.child("imagesPost/"+imagePath)




        //seçtiğimiz resmi bitmap formatına çeviriyoruz
        val baos = ByteArrayOutputStream()

        //ardından bu gelen image bir sıkıştırma yapıyoruz
        bitmap!!.compress(Bitmap.CompressFormat.JPEG,100,baos)

        //sisteme atabilmek için image son olarak byte çeviriyoruz
        val data = baos.toByteArray()
        val uploadTask = ImageRef.putBytes(data)

        uploadTask.addOnFailureListener{
            //yükleme sırasında hata olur ise bu durumu ekrana basıyoruz
            Toast.makeText(this,"Yükleme sırasında hata", Toast.LENGTH_SHORT).show()

        }.addOnSuccessListener {
            taskSnapshot ->
            DowloadURL =taskSnapshot.downloadUrl!!.toString()

        }


    }
    private fun  SplitString(email: String): String {
        //split fonksiyonu ile parçalayıp ilk parçasını geri döndürüyoruz
        return  email.split("0")[0];
    }
}
