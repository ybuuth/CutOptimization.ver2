package com.example.cut_optimization;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CutOptimizationApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
	void contextLoads() {
	}

    @Test
    @Disabled
    public void optimizationNoPartialSheetsNoDisableRotationNoExpressCalc() throws Exception {
        String body = """
                {
                  "details": [
                    {
                      "height": 370,
                      "width": 667,
                      "quantity": 1,
                      "typeMaterial": "1"
                    },
                    {
                      "height": 175,
                      "width": 576,
                      "quantity": 8,
                      "typeMaterial": "3"
                    },
                    {
                      "height": 294,
                      "width": 550,
                      "quantity": 3,
                      "typeMaterial": "1"
                    }
                  ],
                  "sawCutWidth": 4,
                  "workpieces": [
                    {
                      "height": 1263,
                      "width": 1091,
                      "quantity": 1,
                      "typeMaterial": "1"
                    },
                    {
                      "height": 1337,
                      "width": 1327,
                      "quantity": 1,
                      "typeMaterial": "4"
                    },
                    {
                      "height": 1486,
                      "width": 1303,
                      "quantity": 1,
                      "typeMaterial": "2"
                    },
                    {
                      "height": 1227,
                      "width": 1354,
                      "quantity": 1,
                      "typeMaterial": "3"
                    }
                  ],
                  "endlessWorkpieces": [
                    {
                      "height": 1500,
                      "width": 1500,
                      "typeMaterial": "1"
                    },
                    {
                      "height": 1500,
                      "width": 1500,
                      "typeMaterial": "2"
                    },
                    {
                      "height": 1500,
                      "width": 1500,
                      "typeMaterial": "3"
                    },
                    {
                      "height": 1500,
                      "width": 1500,
                      "typeMaterial": "4"
                    }
                  ],
                  "typesOfMaterial": [
                    {
                      "name": "1"
                    },
                    {
                      "name": "2"
                    },
                    {
                      "name": "3"
                    },
                    {
                      "name": "4"
                    }
                  ],
                  "initialTemperature": 10,
                  "usePartialSheets": false,
                  "disableRotation": false,
                  "isExpressCalculation": false
                }""";

        mockMvc.perform(post("/optimization")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
