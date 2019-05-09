package io.monix.circe

import cats.effect._
import minitest.TestSuite
import monix.execution.schedulers.TestScheduler

object MainSpec extends TestSuite[TestScheduler] {

  def setup(): TestScheduler = TestScheduler()

  def tearDown(env: TestScheduler): Unit = {
    assert(env.state.tasks.isEmpty, "should not have tasks left to execute")
  }

  test("Main runs a println") { implicit s =>
    assertEquals(Main.run(List.empty[String]).runSyncUnsafe(), ExitCode.Success)
  }
}
