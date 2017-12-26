import org.scalatest.FlatSpec

class AnaliticUtilSpec extends FlatSpec {

  "Test kcal sum" should "eating 1 (3 products) + eating 2 (2 products)" in {
    val text = "100 картофель\n90 тунец\n40 соус\n\n180 молоко\n190 прот батончик"
    val kcalCount = 100 + 90 + 40 + 180 + 190
    assert(AnaliticUtil.kcalCount(text) == kcalCount)
  }
  it should "eating 1 (1 product)" in {
    assert(AnaliticUtil.kcalCount("190 прот батончик") == 190)
  }
  it should "text with free records" in {
    val text = "2-й прием пищи\n 100 картофель\n90 тунец\n40 соус\n\n3-й пп\n180 молоко\n190 прот батончик"
    val kcalCount = 100 + 90 + 40 + 180 + 190
    assert(AnaliticUtil.kcalCount(text) == kcalCount)
  }
  it should "text without kcal information" in {
    val text = "картофель\nтунец\nсоус\n\n\nмолоко\nпрот батончик"
    assert(AnaliticUtil.kcalCount(text) == 0)
  }
  it should "empty text" in {
    assert(AnaliticUtil.kcalCount("  \n\n \n   \t \n\r  ") == 0)
    assert(AnaliticUtil.kcalCount("") == 0)
  }
}
