package com.zenmo.vallum;

import com.zenmo.vallum.Vallum;
import com.zenmo.zummon.companysurvey.Address;
import com.zenmo.zummon.companysurvey.GridConnection;
import com.zenmo.zummon.companysurvey.PandID;
import com.zenmo.zummon.companysurvey.Survey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VallumTest {
    @Test
    public void test() {
        var port = 8083;
        var stopZtor = InitKt.initZtor(port);

        Vallum vallum = new Vallum(
                System.getenv("CLIENT_ID"),
                System.getenv("CLIENT_SECRET"),
                "http://localhost:" + port,
                "https://keycloak.zenmo.com/realms/testrealm/protocol/openid-connect/token"
        );

        List<Survey> waardkwartierSurveys = vallum.getSurveysByProject("Waardkwartier");
        assertEquals(1, waardkwartierSurveys.size());

        List<Survey> hessenwiekSurveys = vallum.getSurveysByProject("Hessenwiek");
        // user does not have access to this project
        assertEquals(0, hessenwiekSurveys.size());


        var firstSurvey = waardkwartierSurveys.get(0);
        Address firstAddress = firstSurvey.getAddresses().get(0);
        int houseNumber = firstAddress.getHouseNumber();
        assertEquals(35, houseNumber);

        GridConnection gc = firstAddress.getGridConnections().get(0);
        Set<PandID> pandIds = gc.getPandIds();
        assertEquals("[PandID(value=1234567890123456), PandID(value=6543210987654321)]", pandIds.toString());

        var timeSeries = gc.getElectricity().getQuarterHourlyFeedIn_kWh();

        assertTrue(timeSeries.getValues().length > 0);

        // example usage
        for (var survey: waardkwartierSurveys) {
            for (var address: survey.getAddresses()) {
                for (var gridConnection: address.getGridConnections()) {
                    var pandIds2 = gridConnection.getPandIds();
                    var installedPv_kWp = gridConnection.getSupply().getPvInstalledKwp();
                    // hydrate model with vars
                }
            }
        }

//        Instant javaStart = timeSeries.getStart().toJavaInstant();
//        System.out.println(javaStart);
//        System.out.println(javaStart.isBefore(Instant.parse("2023-01-01T01:00:00+01:00")));

        stopZtor.stop();
    }

}
