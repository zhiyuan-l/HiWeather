import com.vito.work.weather.service.LocationService
import com.vito.work.weather.web.controllers.WebLocationController
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean



/**
 * @author       zhiyuan
 */

@RunWith(SpringRunner::class)
@WebMvcTest(WebLocationController::class)
class DemoApplicationTests {

    @Autowired
    private val mvc: MockMvc? = null

    @MockBean
    private lateinit var locationService: LocationService

    @Test
    fun locations() {

    }

}