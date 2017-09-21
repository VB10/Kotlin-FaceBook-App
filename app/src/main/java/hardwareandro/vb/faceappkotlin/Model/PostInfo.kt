package hardwareandro.vb.faceappkotlin.Model

/**
 * Created by vb on 21/09/2017.
 */
class PostInfo {

    //user text ve resim propertyleri
    var UserUID:String?=null
    var Text:String?=null
    var PostImage:String?=null

    //gelen değerleri bizim propertlerimze atıyoruz
    constructor(userUID:String?,text:String,postImage:String?){
        this.UserUID=userUID
        this.PostImage=postImage
        this.Text=text
    }

}