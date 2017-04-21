package com.clara.velocify

import com.expeditelabs.util.StatsObserver
import com.expeditelabs.util.services.{ConfiguredService, ExpediteHttp}
import com.twitter.finagle.service.{RetryBudget, RetryExceptionsFilter, RetryPolicy}
import com.twitter.finagle.stats.StatsReceiver
import com.twitter.finagle.http
import com.twitter.logging.Logger
import com.twitter.util.{Future, JavaTimer, Try}

object VelocifyClient {
  def velocifyBacked(
    statsReceiver: StatsReceiver
  ): VelocifyClient = {
    val retryFilter = mkRetryFilter(3, statsReceiver.scope("retry_filter").scope("velocify"))
    val httpService = ExpediteHttp.mkHttpService(VelocifyClientConfig.Host, 443, "velocify")

    val configuredService = ConfiguredService(
      service = retryFilter andThen httpService,
      config = VelocifyClientConfig()
    )
    new VelocifyBackedVelocifyClient(configuredService, statsReceiver)
  }

  def nullClient(statsReceiver: StatsReceiver): VelocifyClient =
    new NullVelocifyClient(statsReceiver)

  private[this] def mkRetryFilter(
    numRetries: Int,
    scopedReceiver: StatsReceiver
  ): RetryExceptionsFilter[http.Request, http.Response] =
    new RetryExceptionsFilter[http.Request, http.Response](
      RetryPolicy.tries[Try[Nothing]](numRetries, RetryPolicy.WriteExceptionsOnly),
      new JavaTimer(isDaemon = true),
      scopedReceiver,
      RetryBudget()
    )
}

class VelocifyBackedVelocifyClient(
  velocifyApi: ConfiguredService[http.Request, http.Response, VelocifyClientConfig],
  statsReceiver: StatsReceiver
) extends VelocifyClient with StatsObserver {

  override val scopedStats    = statsReceiver.scope("velocifyClient")
  override val requestLogger  = Logger("velocifyClient")

  def apply(payload: VelocifyPayload): Future[Unit] = {
    observe("apply") {
      velocifyApi.service(mkRequest(payload)).unit
    }
  }

  private[this] def mkRequest(payload: VelocifyPayload) = {
    val formParams = payload.toMap.collect {
      case (key, Some(value)) => http.SimpleElement(key, value)
    }

    val req = http.RequestBuilder()
      .add(formParams.toSeq)
      .url(velocifyApi.config.url)
      .buildFormPost()

    requestLogger.info(s"Posting: ${req.contentString}")

    req
  }
}

class NullVelocifyClient(statsReceiver: StatsReceiver) extends VelocifyClient with StatsObserver {
  override val scopedStats    = statsReceiver.scope("nullVelocifyClient")
  override val requestLogger  = Logger("nullVelocifyClient")

  def apply(payload: VelocifyPayload): Future[Unit] = {
    observe("apply") {
      requestLogger.info(s"Sample Post: ${payload.toMap}")
      Future.Done
    }
  }
}
