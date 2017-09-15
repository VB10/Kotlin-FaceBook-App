package hardwareandro.vb.faceappkotlin

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import hardwareandro.vb.faceappkotlin.Model.Data
import kotlinx.android.synthetic.main.activity_home_page.*

class HomePage : AppCompatActivity() {

    var FacePostList = ArrayList<Data>()
    var adapter:MyFaceAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)


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
}
