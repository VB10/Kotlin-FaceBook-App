package hardwareandro.vb.faceappkotlin.Model

class Data {

    //atılan postun özel adresi
    var postID:String?=null
    //postun içeriği
    var postText:String?=null
    //postun image adresi
    var postImageUrl:String?=null
    //postu atan kullanıcı adresi
    var postPersonUID:String?=null


    //encapsüle halindede yazabilirdik ama hızlı olması için bu şekilde kullandık
    constructor(postID:String,postText:String,postImageUrl:String,postPersonUID:String){
        this.postID=postID
        this.postText=postText
        this.postImageUrl=postImageUrl
        this.postPersonUID=postPersonUID

    }
}