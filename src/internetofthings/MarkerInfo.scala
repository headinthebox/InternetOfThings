package internetofthings

object MarkerInfo {
  def apply(lat: Double, lng: Double, message: String): MarkerInfo = {
    new MarkerInfo (lat,lng, message)
  }
}

class MarkerInfo(val lat: Double, val lng: Double, val message: String ) {}
