package hardwareandro.vb.faceappkotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    //login yapabilmek için bağlantıyı kuruyoruz
    private  var mAuth:FirebaseAuth?=null

    private  var database= FirebaseDatabase.getInstance()
    private  var myRef = database.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mAuth= FirebaseAuth.getInstance()



        //img buttonuna basıldıktan sonra olacaklar
        imgPerson.setOnClickListener(View.OnClickListener {


            //img okurken istek atıyoruz

            checkPermisson()
        })
    }


    val READIMAGE:Int =10
    fun checkPermisson(){

        if (Build.VERSION.SDK_INT>=23) {

            //burada hangi sayfada ve izin isterken ne yazmanız gerektiğini girmelisiniz
            //bu izinin önceden alınıp alınmadığını sormaktayız
            if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    !=  PackageManager.PERMISSION_GRANTED){
                //bir istek oluşturup bu isteğin sonucunda bizim yukarıda 10 sonucumuzun dönüp dönmemesine bakıyoruz
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),READIMAGE)
                return
            }
        }

        loadImage()

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        //bu istek sonucunda bize gelen cevap istediğimiz ise işleme başlıyoruz
        when(requestCode) {

            READIMAGE-> {
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED) loadImage()
                else Toast.makeText(this,"Cannot access your images",Toast.LENGTH_SHORT).show()


            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    //burada random bir değer oluşturuyoruz doğru ise arkada yakalamak için
    var PICK_IMAGE_CODE:Int = 15
    private fun loadImage() {

        //bir istek atıyoruz ve cihazdan fotoğraf okumak için
        var intent = Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //intent den gelen değer ve içinde birşey olup olmamasını kontrol ediyoruz
        if (requestCode==PICK_IMAGE_CODE && data!=null) {

            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor =contentResolver.query(selectedImage,filePathColumn, null,null,null)
            cursor.moveToFirst()

            val columnIndex =cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            imgPerson.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }


        super.onActivityResult(requestCode, resultCode, data)
    }


    fun LoginBtn(view: View) {
        //login için firebase istek atıyoruz

        LoginFirebase(etUsername.text.toString(),
                        etPassword.text.toString())
    }

    private fun  LoginFirebase(user: String, pass: String) {

        //normalde direk pass kontrol edilip yapıyoruz
        //şuan projede girilen mail adresini direk kabul edip oluşturup giriyoruz
        mAuth!!.createUserWithEmailAndPassword(user,pass).addOnCompleteListener(this){

            task ->
            if (task.isSuccessful) {
                Toast.makeText(this,"Hesap Başarılıyla Oluşturuldu",Toast.LENGTH_SHORT).show()
                //oluşturduğumuz userin bilgisini çekiyoruz
                SaveImageFirebase()


            }
        }
    }

    fun SaveImageFirebase(){
        //login olduktan sonra user biglilerini yakalıyoruz
        var currentUser = mAuth!!.currentUser

        //sistemde login olmuş kişinin mail adresini alıyoruz
        val email:String = currentUser!!.email.toString()

        //storage bağlantı sağlıyoruz
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://faceappskotlin.appspot.com")

        //date yi alarak profil resminde bu stringi kullanacağız
        val df =SimpleDateFormat("ddMMyyHHmmss")
        val dataObj= Date()
        //@den öncesini alıp kişinin ismine göre kayıt ediyoruz
        val imagePath=SplitString(email) + "." + df.format(dataObj)+".jpg"

        val ImageRef = storageRef.child("images/"+imagePath)


        imgPerson.isDrawingCacheEnabled=true
        imgPerson.buildDrawingCache()

        //seçtiğimiz resmi bitmap formatına çeviriyoruz
        val drawable = imgPerson.drawable as BitmapDrawable
        val bitmap =drawable.bitmap
        val baos =ByteArrayOutputStream()

        //ardından bu gelen image bir sıkıştırma yapıyoruz
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)

        //sisteme atabilmek için image son olarak byte çeviriyoruz
        val data = baos.toByteArray()
        val uploadTask = ImageRef.putBytes(data)

        uploadTask.addOnFailureListener{
            //yükleme sırasında hata olur ise bu durumu ekrana basıyoruz
            Toast.makeText(this,"Yükleme sırasında hata",Toast.LENGTH_SHORT).show()

        }.addOnSuccessListener {
            taskSnapshot ->
            var DowloadUrl =taskSnapshot.downloadUrl!!.toString()
            //başarı bir kayıt ise bunları önce stroage atıyoruz
            //arından databasemizde mail ve bu img url olduğu bir child oluşturuyoruz
            myRef.child("Users").child(currentUser.uid).child("email").setValue(currentUser.email)
            myRef.child("Users").child(currentUser.uid).child("profileImages").setValue(DowloadUrl)

            //kişinin bu bilgilerine erişip LoadPost metodumuzu çağırıyoruz
            //gerekli kontrolleri yapıp yeni sayfamıza erişiyoruz
            LoadPost()
        }




    }

    fun LoadPost() {
        var currentUser = mAuth!!.currentUser

        if (mAuth!=null){
            var intent =Intent(this,HomePage::class.java)
            intent.putExtra("email",currentUser!!.email)
            intent.putExtra("uid",currentUser!!.uid)

            startActivity(intent)
        }
    }
    private fun  SplitString(email: String): String {
        //split fonksiyonu ile parçalayıp ilk parçasını geri döndürüyoruz
        return  email.split("0")[0];
    }


}
