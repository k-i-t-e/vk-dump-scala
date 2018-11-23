package services

import java.util.concurrent.ConcurrentHashMap

import com.google.inject.{Inject, Singleton}
import model.VkUser
import security.UserDetailsService
import services.client.VkClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VkClientService @Inject()(userDetailsService: UserDetailsService)(implicit ec: ExecutionContext) {
  private val clientMap = new ConcurrentHashMap[Long, VkClient]()
  
  def getClient(userId: Long): Future[VkClient] = {
    Option(clientMap.get(userId)).fold(userDetailsService.load(userId).map(u => {
      val c = new VkClient(u, 60000, 5)
      clientMap.put(userId, c)
      c
    }))(c => Future.successful(c))
  }

  def getClient(user: VkUser): VkClient = {
    Option(clientMap.get(user.id)).fold({
      val c = new VkClient(user, 60000, 5)
      clientMap.put(user.id, c)
      c
    })(c => c)
  }

  def removeClient(user: VkUser): Unit = {
    clientMap.remove(user.id)
  }
}
