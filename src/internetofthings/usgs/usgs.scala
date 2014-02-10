package internetofthings.usgs

import retrofit.http.GET
import retrofit.{RetrofitError, RestAdapter, Callback}
import rx.lang.scala.Observable
import rx.lang.scala.subjects.AsyncSubject
import retrofit.client.Response
import com.google.gson.annotations.SerializedName
import java.util.Date
import scala.language.postfixOps
import scala.concurrent.duration._

object Usgs {

  private val restAdapter = new RestAdapter.Builder().setServer("http://earthquake.usgs.gov").build()

  def getEarthQuakeStream(refresh: Duration = 10 seconds): Observable[Feature] = {
    (Observable.items(new FeatureCollection()) ++ Observable.timer(initialDelay = 0 seconds, period = refresh).flatMap(_ => Usgs()))
    .buffer(2,1).map((b: Seq[FeatureCollection]) => {
      val diff: Map[String, Feature] = b(1).features -- b(0)._features.map(f => f.id)
      println("___Diff______")
      println(diff)
      println("_____________")
      new FeatureCollection {
        override val _features: Array[Feature] = diff.values.toArray
        override val metadata: MetaData = b(1).metadata
      }
    }).flatMap(x => Observable.from(x._features)).doOnEach(_ => { Thread.sleep(1000) })
  }

  def apply(): Observable[FeatureCollection] = {

    val subject = AsyncSubject[FeatureCollection]()

    restAdapter.create(classOf[Usgs]).get(new Callback[FeatureCollection] {

      def failure(error: RetrofitError): Unit = {
        subject.onError(error.getCause)
      }

      def success(t: FeatureCollection, response: Response): Unit = {
        subject.onNext(t)
        subject.onCompleted()
      }

    })

    subject
  }
}

private trait Usgs {
  @GET("/earthquakes/feed/geojson/all/day")
  def get(callback: Callback[FeatureCollection])
}

class Feature {

  val properties : Properties = new Properties ()
  val geometry: Point         = new Point()
  val id: String              = ""

  override def toString() = s"{ 'id': ${id}, 'properties':'${properties}', 'geometry':'${geometry}' }";
}

class Properties {
  val place: String           = null
  @SerializedName("time")
  private val _time: Long     = 0L
  @transient
  lazy val time: Date         = new Date(_time)
  @SerializedName("updated")
  private val _updated: Long  = 0L
  @transient
  lazy val updated: Date      = new Date(_updated)
  @SerializedName("mag")
  val magnitude: Double       = 0D
  val detail: String          = null
  val felt: Int               = 0
  val cdi: Double             = 0D
  val mmi: Double             = 0D
  val alert: String           = null
  val status: String          = null
  val tsunami: Int            = 0
  val sig: Int                = 0
  val net: String             = null
  val code: String            = null
  val ids: String             = null
  val sources: String         = null
  val types: String           = null
  val nst: Int                = 0
  val dmin: Double            = 0D
  val rms: Double             = 0D
  val gap: Double             = 0D
  val magType: String         = null
  val `type`: String          = null

  // add fields that you want to see.
  override def toString() = s"{ 'time':'${time}', 'place':'${place}', 'magnitude':'${magnitude}' }";

}

class Point {
  val coordinates: Array[Double] = Array[Double]()

  lazy val latitude: Double              = coordinates(1)
  lazy val longitude  : Double           = coordinates(0)
  lazy val altitude  : Double            = coordinates(2)

  override def toString() = s"{ 'longitude':'${longitude}', 'latitude':'${latitude}', 'altitude':'${altitude}' }";

}

class FeatureCollection {
  val metadata : MetaData = new MetaData()

  @SerializedName("features")
  val _features : Array[Feature] = Array[Feature]()
  @transient
  lazy val features: Map[String, Feature] =  _features.map(f => (f.id, f)).toMap

  override def toString() = s"{ 'metadata':'${metadata}',\n 'features':[${_features.map(_.toString()).reduceLeft((x,s)=> s"$x,\n $s")}] }";
}

class MetaData {

  @SerializedName("generated")
  val _generated: Long     = 0L
  @transient
  lazy val generated: Date = new Date(_generated)
  val url: String          = null
  val title: String        = null
  val api: String          = null
  val count: Int           = 0
  val status: Int          = 0

  override def toString() = s"{ 'generated': '$generated', 'url':'$url', 'title':'$title', 'api': '$api', 'count': '$count', 'status': '$status' }";
}

object Magnitude extends Enumeration {

  type Magnitude = Value
  val Micro, Minor, Light, Moderate, Strong, Major, Great = Value

  def apply(magnitude: Double): Magnitude = {

    if(magnitude >= 8.0) return Great
    if(magnitude >= 7.0) return Major
    if(magnitude >= 6.0) return Strong
    if(magnitude >= 5.0) return Moderate
    if(magnitude >= 4.0) return Light
    if(magnitude >= 3.0) return Minor
    return Micro

  }
}