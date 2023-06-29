package care.smith.top.top_phenotypic_query.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.Entity;
import care.smith.top.model.Phenotype;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Divide;
import care.smith.top.top_phenotypic_query.c2reasoner.functions.arithmetic.Power;
import care.smith.top.top_phenotypic_query.converter.csv.CSV;
import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Phe;
import care.smith.top.top_phenotypic_query.util.builder.Res;
import care.smith.top.top_phenotypic_query.util.builder.Val;
import org.junit.jupiter.api.Test;

public class CSVTest {

  private static Phenotype age = new Phe("age", "http://loinc.org", "30525-0").number("a").get();
  private static Phenotype young = new Phe("young").restriction(age, Res.lt(18)).get();
  private static Phenotype old = new Phe("old").restriction(age, Res.ge(18)).get();

  private static Phenotype sex = new Phe("sex", "http://loinc.org", "46098-0").string().get();
  private static Phenotype female = new Phe("female").restriction(sex, Res.of("female")).get();
  private static Phenotype male = new Phe("male").restriction(sex, Res.of("male")).get();

  private static Phenotype weight =
      new Phe("weight", "http://loinc.org", "3141-9")
          .code("http://snomed.info/sct", "27113001")
          .number("kg")
          .titleDe("Gewicht")
          .titleEn("Weight")
          .synonymDe("Gewicht Synonym 1")
          .synonymDe("Gewicht Synonym 2")
          .synonymEn("Weight Synonym 1")
          .synonymEn("Weight Synonym 2")
          .descriptionDe("Gewicht Description 1")
          .descriptionDe("Gewicht Description 2")
          .descriptionEn("Weight Description 1")
          .descriptionEn("Weight Description 2")
          .get();

  private static Phenotype height =
      new Phe("height", "http://loinc.org", "3137-7")
          .code("http://snomed.info/sct", "1153637007")
          .number("m")
          .get();

  private static Phenotype bmi =
      new Phe("bmi", "http://loinc.org", "39156-5")
          .number()
          .expression(
              Divide.of(Exp.ofEntity("weight"), Power.of(Exp.ofEntity("height"), Exp.of(2))))
          .get();
  private static Phenotype overweight = new Phe("overweight").restriction(bmi, Res.gt(25)).get();

  private static Entity[] entities = {
    age, young, old, sex, female, male, weight, height, bmi, overweight
  };

  @Test
  public void testMetadata1() {
    String metadataRequired =
        "phenotype;parent;type;itemtype;datatype;unit;titles;synonyms;descriptions;codes;restriction;expression"
            + System.lineSeparator()
            + "age;;single_phenotype;observation;number;a;;;;http://loinc.org|30525-0;;"
            + System.lineSeparator()
            + "young;age;single_restriction;observation;boolean;;;;;;|MIN|1|< 18|;"
            + System.lineSeparator()
            + "old;age;single_restriction;observation;boolean;;;;;;|MIN|1|>= 18|;"
            + System.lineSeparator()
            + "sex;;single_phenotype;observation;string;;;;;http://loinc.org|46098-0;;"
            + System.lineSeparator()
            + "female;sex;single_restriction;observation;boolean;;;;;;|MIN|1|[female]|;"
            + System.lineSeparator()
            + "male;sex;single_restriction;observation;boolean;;;;;;|MIN|1|[male]|;"
            + System.lineSeparator()
            + "weight;;single_phenotype;observation;number;kg;Gewicht|de,Weight|en;Gewicht Synonym"
            + " 1|de,Gewicht Synonym 2|de,Weight Synonym 1|en,Weight Synonym 2|en;Gewicht"
            + " Description 1|de,Gewicht Description 2|de,Weight Description 1|en,Weight"
            + " Description 2|en;http://loinc.org|3141-9,http://snomed.info/sct|27113001;;"
            + System.lineSeparator()
            + "height;;single_phenotype;observation;number;m;;;;http://loinc.org|3137-7,http://snomed.info/sct|1153637007;;"
            + System.lineSeparator()
            + "bmi;;composite_phenotype;observation;number;;;;;http://loinc.org|39156-5;;(weight /"
            + " (height ^ [2]))"
            + System.lineSeparator()
            + "overweight;bmi;composite_restriction;observation;boolean;;;;;;|MIN|1|> 25|;"
            + System.lineSeparator();

    String metadataActual = new CSV().toString(entities);

    //    System.out.println(metadataActual);
    //
    //    try {
    //      new CSV().write(entities, new FileOutputStream("test_files/metadata.csv"));
    //    } catch (FileNotFoundException e) {
    //      e.printStackTrace();
    //    }

    assertEquals(metadataRequired, metadataActual);
  }

  @Test
  public void testMetadata2() {
    String metadataRequired =
        "phenotype,parent,type,itemtype,datatype,unit,titles,synonyms,descriptions,codes,restriction,expression"
            + System.lineSeparator()
            + "age,,single_phenotype,observation,number,a,,,,http://loinc.org|30525-0,,"
            + System.lineSeparator()
            + "young,age,single_restriction,observation,boolean,,,,,,|MIN|1|< 18|,"
            + System.lineSeparator()
            + "old,age,single_restriction,observation,boolean,,,,,,|MIN|1|>= 18|,"
            + System.lineSeparator()
            + "sex,,single_phenotype,observation,string,,,,,http://loinc.org|46098-0,,"
            + System.lineSeparator()
            + "female,sex,single_restriction,observation,boolean,,,,,,|MIN|1|[female]|,"
            + System.lineSeparator()
            + "male,sex,single_restriction,observation,boolean,,,,,,|MIN|1|[male]|,"
            + System.lineSeparator()
            + "weight,,single_phenotype,observation,number,kg,Gewicht|de::Weight|en,Gewicht Synonym"
            + " 1|de::Gewicht Synonym 2|de::Weight Synonym 1|en::Weight Synonym 2|en,Gewicht"
            + " Description 1|de::Gewicht Description 2|de::Weight Description 1|en::Weight"
            + " Description 2|en,http://loinc.org|3141-9::http://snomed.info/sct|27113001,,"
            + System.lineSeparator()
            + "height,,single_phenotype,observation,number,m,,,,http://loinc.org|3137-7::http://snomed.info/sct|1153637007,,"
            + System.lineSeparator()
            + "bmi,,composite_phenotype,observation,number,,,,,http://loinc.org|39156-5,,(weight /"
            + " (height ^ [2]))"
            + System.lineSeparator()
            + "overweight,bmi,composite_restriction,observation,boolean,,,,,,|MIN|1|> 25|,"
            + System.lineSeparator();

    String metadataActual =
        new CSV()
            .charset("UTF-16")
            .entriesDelimiter(",")
            .entryPartsDelimiter("::")
            .toString(entities);

    assertEquals(metadataRequired, metadataActual);
  }

  @Test
  public void testMetadata3() {
    String metadataRequired =
        "phenotype,parent,type,itemtype,datatype,unit,titles,synonyms,descriptions,codes,restriction,expression"
            + System.lineSeparator()
            + "age,,single_phenotype,observation,number,a,,,,http://loinc.org|30525-0,,"
            + System.lineSeparator()
            + "young,age,single_restriction,observation,boolean,,,,,,|MIN|1|< 18|,"
            + System.lineSeparator()
            + "old,age,single_restriction,observation,boolean,,,,,,|MIN|1|>= 18|,"
            + System.lineSeparator()
            + "sex,,single_phenotype,observation,string,,,,,http://loinc.org|46098-0,,"
            + System.lineSeparator()
            + "female,sex,single_restriction,observation,boolean,,,,,,|MIN|1|[female]|,"
            + System.lineSeparator()
            + "male,sex,single_restriction,observation,boolean,,,,,,|MIN|1|[male]|,"
            + System.lineSeparator()
            + "weight,,single_phenotype,observation,number,kg,Gewicht|de::Weight|en,Gewicht Synonym"
            + " 1|de::Gewicht Synonym 2|de::Weight Synonym 1|en::Weight Synonym 2|en,Gewicht"
            + " Description 1|de::Gewicht Description 2|de::Weight Description 1|en::Weight"
            + " Description 2|en,http://loinc.org|3141-9::http://snomed.info/sct|27113001,,"
            + System.lineSeparator()
            + "height,,single_phenotype,observation,number,m,,,,http://loinc.org|3137-7::http://snomed.info/sct|1153637007,,"
            + System.lineSeparator()
            + "bmi,,composite_phenotype,observation,number,,,,,http://loinc.org|39156-5,,(weight /"
            + " (height ^ [2]))"
            + System.lineSeparator()
            + "overweight,bmi,composite_restriction,observation,boolean,,,,,,|MIN|1|> 25|,"
            + System.lineSeparator();

    DataAdapterConfig config = DataAdapterConfig.getInstanceFromResource("config/CSV_Adapter.yml");

    assertEquals("UTF-16", config.getCsvSettings().getCharset());
    assertEquals(",", config.getCsvSettings().getEntriesDelimiter());
    assertEquals("::", config.getCsvSettings().getEntryPartsDelimiter());

    String metadataActual = new CSV(config).toString(entities);

    assertEquals(metadataRequired, metadataActual);
  }

  @Test
  public void testData() {
    ResultSet rs = new ResultSet();

    rs.addValueWithRestriction("1", young, Val.of(15));
    rs.addValueWithRestriction("1", male, Val.of("male"));
    rs.addValue(
        "1",
        weight,
        Res.geLe(DateUtil.parse("2008-01-01"), DateUtil.parse("2008-12-31")),
        Val.of(58, DateUtil.parse("2008-01-01")));
    rs.addValue(
        "1",
        weight,
        Res.geLe(DateUtil.parse("2009-01-01"), DateUtil.parse("2009-12-31")),
        Val.of(59, DateUtil.parse("2009-01-01")));
    rs.addValue(
        "1",
        weight,
        Res.geLe(DateUtil.parse("2010-01-01"), DateUtil.parse("2010-12-31")),
        Val.of(60, DateUtil.parse("2010-01-01")));
    rs.addValue("1", height, null, Val.of(1.8));
    rs.addValue("1", bmi, null, Val.of(18.52));

    rs.addValueWithRestriction("2", old, Val.of(35));
    rs.addValueWithRestriction("2", female, Val.of("female"));
    rs.addValue("2", weight, null, Val.of(80));
    rs.addValue("2", height, null, Val.of(1.65));
    rs.addValueWithRestriction("2", overweight, Val.of(29.38));

    String dataRequired =
        "subject;phenotype;timestamp;number_value;string_value;date_time_value;boolean_value"
            + System.lineSeparator()
            + "1;age;;15;;;"
            + System.lineSeparator()
            + "1;young;;;;;true"
            + System.lineSeparator()
            + "1;sex;;;male;;"
            + System.lineSeparator()
            + "1;male;;;;;true"
            + System.lineSeparator()
            + "1;weight;2008-01-01T00:00:00;58;;;"
            + System.lineSeparator()
            + "1;weight;2009-01-01T00:00:00;59;;;"
            + System.lineSeparator()
            + "1;weight;2010-01-01T00:00:00;60;;;"
            + System.lineSeparator()
            + "1;height;;1.8;;;"
            + System.lineSeparator()
            + "1;bmi;;18.52;;;"
            + System.lineSeparator()
            + "2;age;;35;;;"
            + System.lineSeparator()
            + "2;old;;;;;true"
            + System.lineSeparator()
            + "2;sex;;;female;;"
            + System.lineSeparator()
            + "2;female;;;;;true"
            + System.lineSeparator()
            + "2;weight;;80;;;"
            + System.lineSeparator()
            + "2;height;;1.65;;;"
            + System.lineSeparator()
            + "2;bmi;;29.38;;;"
            + System.lineSeparator()
            + "2;overweight;;;;;true"
            + System.lineSeparator();

    String dataActual = new CSV().toString(rs);

    //    System.out.println(dataActual);
    //
    //    try {
    //      new CSV().write(rs, new FileOutputStream("test_files/data.csv"));
    //    } catch (FileNotFoundException e) {
    //      e.printStackTrace();
    //    }

    assertEquals(dataRequired, dataActual);
  }
}
