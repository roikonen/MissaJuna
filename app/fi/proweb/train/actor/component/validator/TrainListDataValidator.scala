package fi.proweb.train.actor.component.validator

import fi.proweb.train.actor.component.DataValidator

class TrainListDataValidator extends DataValidator {

  def valid(loadData: String): Boolean = {
    loadData.startsWith("""<rss version="2.0"><channel xmlns:georss="http://www.georss.org/georss"><title>""")
  }
  
}