import internetofthings._
import internetofthings.usgs._
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import rx.lang.scala._

object Main {
  def main(args: Array[String]) {
    val main = new Main ()
    main.launch()
  }
}

class Main extends Application {

  val source: Observable[MarkerInfo] = {
    val quakes: Observable[Feature] = Usgs.getEarthQuakeStream()
    quakes.doOnEach(f => println(s"${f.properties.magnitude} @ ${f.properties.place}"))
      .filter(f => f.properties.magnitude > 2.0)
      .map(f =>
      MarkerInfo(f.geometry.latitude, f.geometry.longitude, f.properties.magnitude.toString))
  }

  def launch() = { Application.launch() }

  def start(stage: Stage) {

    val map: Map = new Map()

    map.onLoaded.subscribe(o => new Thread {
      override def run(): Unit =  source.subscribe(o)
    }.start())

    val scene: Scene = new Scene(map)
    stage.setScene(scene)
    stage.setWidth(800)
    stage.setHeight(600)
    stage.show
  }
}

