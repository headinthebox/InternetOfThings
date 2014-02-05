package internetofthings

import javafx.scene.layout.Region
import rx.lang.scala.{Observable, Observer}
import javafx.beans.value.{ObservableValue, ChangeListener}
import javafx.concurrent.Worker
import netscape.javascript.JSException
import javafx.application.Platform
import rx.lang.scala.subjects.AsyncSubject
import javafx.scene.web.{WebEngine, WebView}

class Map extends Region with Observer[MarkerInfo] {

  private val subject: AsyncSubject[Observer[MarkerInfo]] = AsyncSubject()
  private[internetofthings] var webView: WebView = new WebView
  private[internetofthings] var webEngine: WebEngine = webView.getEngine
  val map: String = "file:///Users/Oracle/IdeaProjects/JavaFxPlayGround/src/sample/Map.html"

  webEngine.load(map)
  this.getChildren.add(webView)

  def onLoaded: Observable[Observer[MarkerInfo]] = {

    this.webEngine.getLoadWorker.stateProperty.addListener(new ChangeListener[Worker.State](){
      def changed(p1: ObservableValue[_ <: Worker.State], p2: Worker.State, p3: Worker.State): Unit = {
        subject.onNext(Map.this)
        subject.onCompleted
      }
    })
    return subject
  }

  private def updateValue(lat: Double, lng: Double, message: String) {
    try {
      this.webEngine.executeScript("document.addMarker(" + lat + ", " + lng + ", '" + message + "')")
      return
    }
    catch {
      case e: JSException => {
        System.out.println(e.getMessage)
      }
    }
  }

  override def onCompleted {}

  override def onError(e: Throwable) {}

  override def onNext(info: MarkerInfo) {
    Platform.runLater(new Runnable {
      def run {
        updateValue(info.lat, info.lng, info.message)
      }
    })
  }

}
