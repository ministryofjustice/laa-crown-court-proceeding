package integration;

public class ProsecutionMessageBuilder {


    public static final String CONVICTED = "CONVICTED";
    public static final String AQUITTED = "AQUITTED";
    public static final String PART_CONVICTED = "PART CONVICTED";
    public static final String GUILTY = "GUILTY";
    public static final String NOT_GUILTY = "NOT_GUILTY";

    public static String getSqsMessagePayload(Integer maatId) {
        return """
                {
                   prosecutionCaseId : 998984a0-ae53-466c-9c13-e0c84c1fd581,
                   defendantId: aa07e234-7e80-4be1-a076-5ab8a8f49df5,
                   isConcluded: true,
                   hearingIdWhereChangeOccurred : 908ad01e-5a38-4158-957a-0c1d1a783862,
                    offenceSummary: [
                        """ + getOffenceWithPleaAndVerdict(GUILTY, GUILTY) + """
                ],
                maatId: """ + maatId + """
                      ,metadata: {
                          laaTransactionId: 61600a90-89e2-4717-aa9b-a01fc66130c1
                      }
                  }
                """;
    }

    public static String getSqsMessagePayloadWithPlea(Integer maatId, String plea) {
        return """
                {
                   prosecutionCaseId : 998984a0-ae53-466c-9c13-e0c84c1fd581,
                   defendantId: aa07e234-7e80-4be1-a076-5ab8a8f49df5,
                   isConcluded: true
                ,hearingIdWhereChangeOccurred : 908ad01e-5a38-4158-957a-0c1d1a783862,
                offenceSummary: [
                        {
                            offenceId: ed0e9d59-cc1c-4869-8fcd-464caf770744,
                            offenceCode: PT00011,
                            proceedingsConcluded: true,
                            proceedingsConcludedChangedDate: 2022-02-01,
                            plea: {
                                originatingHearingId: 908ad01e-5a38-4158-957a-0c1d1a783862,
                                value: """ + plea + """
                            ,pleaDate: 2022-02-01
                        }
                    }
                ],
                maatId: """ + maatId + """
                    ,metadata: {
                        laaTransactionId: 61600a90-89e2-4717-aa9b-a01fc66130c1
                    }
                }""";
    }

    public static String getSqsMessagePayloadWithVerdict(Integer maatId, String verdict) {
        return """
                {
                   prosecutionCaseId : 998984a0-ae53-466c-9c13-e0c84c1fd581,
                   defendantId: aa07e234-7e80-4be1-a076-5ab8a8f49df5,
                   isConcluded: true
                   ,hearingIdWhereChangeOccurred : 908ad01e-5a38-4158-957a-0c1d1a783862,
                   offenceSummary: [
                        {
                            offenceId: ed0e9d59-cc1c-4869-8fcd-464caf770744,
                            offenceCode: PT00011,
                            proceedingsConcluded: true,
                            proceedingsConcludedChangedDate: 2022-02-01,
                            verdict: {
                                verdictDate: 2022-02-01,
                                originatingHearingId: 908ad01e-5a38-4158-957a-0c1d1a783862,
                                verdictType: {
                                    description: """ + verdict + """
                                    ,category:  """ + verdict + """
                                    ,categoryType:  """ + verdict + """
                                    ,sequence: 4126,
                                    verdictTypeId: null
                              }
                        }
                    }
                ],
                maatId: """ + maatId + """
                    ,metadata: {
                        laaTransactionId: 61600a90-89e2-4717-aa9b-a01fc66130c1
                    }
                }""";
    }

    public static String getSqsMessagePayloadWithPleaAndVerdict(Integer maatId, String plea, String verdict) {
        return """
                {
                   prosecutionCaseId : 998984a0-ae53-466c-9c13-e0c84c1fd581,
                   defendantId: aa07e234-7e80-4be1-a076-5ab8a8f49df5,
                   isConcluded: true
                   ,hearingIdWhereChangeOccurred : 908ad01e-5a38-4158-957a-0c1d1a783862,
                   offenceSummary: [
                        {
                            offenceId: ed0e9d59-cc1c-4869-8fcd-464caf770744,
                            offenceCode: PT00011,
                            proceedingsConcluded: true,
                            proceedingsConcludedChangedDate: 2022-02-01,
                            plea: {
                                originatingHearingId: 908ad01e-5a38-4158-957a-0c1d1a783862,
                                value: """ + plea + """
                               ,pleaDate: 2022-02-01
                            },
                   verdict: {
                    verdictDate: 2022-02-01,
                    originatingHearingId: 908ad01e-5a38-4158-957a-0c1d1a783862,
                    verdictType: {
                        description: """ + verdict + """
                                     ,category:  """ + verdict + """
                                     ,categoryType:  """ + verdict + """
                                     ,sequence: 4126,
                                     verdictTypeId: null
                            }
                        }
                    }
                ],
                maatId: """ + maatId + """
                    ,metadata: {
                        laaTransactionId: 61600a90-89e2-4717-aa9b-a01fc66130c1
                    }
                }""";
    }

    public static String getSqsMessagePayloadWithMultiplePlea(Integer maatId, String plea) {
        return """
                {
                   prosecutionCaseId : 998984a0-ae53-466c-9c13-e0c84c1fd581,
                   defendantId: aa07e234-7e80-4be1-a076-5ab8a8f49df5,
                   isConcluded: true
                   ,hearingIdWhereChangeOccurred : 908ad01e-5a38-4158-957a-0c1d1a783862,
                   offenceSummary: [
                     """ + getOffenceWithPlea(plea) + """
                     ,""" + getOffenceWithPlea(plea) + """             
                ],
                maatId: """ + maatId + """
                    ,metadata: {
                        laaTransactionId: 61600a90-89e2-4717-aa9b-a01fc66130c1
                    }
                }""";
    }

    public static String getPayloadWithMultiplePleaAndVerdict(Integer maatId, String plea) {
        return """
                {
                   prosecutionCaseId : 998984a0-ae53-466c-9c13-e0c84c1fd581,
                   defendantId: aa07e234-7e80-4be1-a076-5ab8a8f49df5,
                   isConcluded: true
                   ,hearingIdWhereChangeOccurred : 908ad01e-5a38-4158-957a-0c1d1a783862,
                   offenceSummary: [
                     """ + getOffenceWithPlea(plea) + """ 
                     ,""" + getOffenceWithPleaAndVerdict(NOT_GUILTY, NOT_GUILTY) + """       
                   ],
                   maatId: """ + maatId + """
                    ,metadata: {
                        laaTransactionId: 61600a90-89e2-4717-aa9b-a01fc66130c1
                    }
                }
                """;
    }

    private static String getOffenceWithPlea(String plea) {
        return """
                {
                    offenceId: ed0e9d59-cc1c-4869-8fcd-464caf770744,
                    offenceCode: PT00011,
                    proceedingsConcluded: true,
                    proceedingsConcludedChangedDate: 2022-02-01,
                    plea: {
                        originatingHearingId: 908ad01e-5a38-4158-957a-0c1d1a783862,
                        value: """ + plea + """
                            ,pleaDate: 2022-02-01
                        }
                }""";
    }

    private static String getOffenceWithPleaAndVerdict(String plea, String verdict) {
        return """
                {
                    offenceId: ed0e9d59-cc1c-4869-8fcd-464caf770744,
                    offenceCode: PT00011,
                    proceedingsConcluded: true,
                    proceedingsConcludedChangedDate: 2022-02-01,
                    plea: {
                        originatingHearingId: 908ad01e-5a38-4158-957a-0c1d1a783862,
                        value: """ + plea + """
                            ,pleaDate: 2022-02-01
                        },
                    verdict: {
                        verdictDate: 2022-02-01,
                        originatingHearingId: 908ad01e-5a38-4158-957a-0c1d1a783862,
                        verdictType: {
                                 description: """ + verdict + """
                                 ,category:  """ + verdict + """
                                 ,categoryType:  """ + verdict + """
                                 ,sequence: 4126,
                                 verdictTypeId: null
                                 }
                       }                        
                }
                """;
    }

    public static String getExpectedRequest(String outcome, String imprisoned) {
        return "{\"repId\":5635567,\"ccOutcome\":\"" + outcome + "\",\"benchWarrantIssued\":null,\"appealType\":\"ACN\",\"imprisoned\":" + imprisoned + ",\"caseNumber\":\"21GN1208521\",\"crownCourtCode\":\"433\"}";
    }

    public static String getExpectedRequest(String outcome) {
        return "{\"repId\":5635567,\"ccOutcome\":\"" + outcome + "\",\"benchWarrantIssued\":null,\"appealType\":\"ACN\",\"imprisoned\":\"N\",\"caseNumber\":\"21GN1208521\",\"crownCourtCode\":\"433\"}";
    }
}
