package care.smith.top.top_phenotypic_query.tests.intern;

import static org.junit.jupiter.api.Assertions.assertEquals;

import care.smith.top.model.ItemType;
import care.smith.top.model.Phenotype;
import care.smith.top.top_phenotypic_query.result.ResultSet;
import care.smith.top.top_phenotypic_query.tests.intern.fhir.Client;
import care.smith.top.top_phenotypic_query.tests.intern.fhir.Med;
import care.smith.top.top_phenotypic_query.tests.intern.fhir.MedAdm;
import care.smith.top.top_phenotypic_query.tests.intern.fhir.MedReq;
import care.smith.top.top_phenotypic_query.tests.intern.fhir.MedSta;
import care.smith.top.top_phenotypic_query.tests.intern.fhir.Pat;
import care.smith.top.top_phenotypic_query.util.DateUtil;
import care.smith.top.top_phenotypic_query.util.builder.Phe;
import care.smith.top.top_phenotypic_query.util.builder.Que;
import care.smith.top.top_phenotypic_query.util.builder.Res;
import java.util.Set;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;

public class FHIRMedicationTestIntern {

  private static Client client = new Client();

  private static Phenotype sex = new Phe("sex").itemType(ItemType.SUBJECT_SEX).string().get();
  private static Phenotype male = new Phe("male").restriction(sex, Res.of("male")).get();
  private static Phenotype age = new Phe("age").itemType(ItemType.SUBJECT_AGE).number().get();
  private static Phenotype old = new Phe("old").restriction(age, Res.ge(60)).get();
  private static Phenotype med =
      new Phe("med", "http://www.whocc.no/atc", "atc1").itemType(ItemType.MEDICATION).bool().get();

  @Test
  void test() {
    client.clean();

    Reference pat1a = client.add(new Pat("p1a").birthDate("2001-01-01").gender("male"));
    Reference pat1b = client.add(new Pat("p1b").birthDate("2001-01-01").gender("female"));
    Reference pat1c = client.add(new Pat("p1c").birthDate("1951-01-01").gender("male"));
    Reference pat1d = client.add(new Pat("p1d").birthDate("1951-01-01").gender("female"));
    Reference pat1e = client.add(new Pat("p1e").birthDate("1951-01-01").gender("male"));

    Reference pat2a = client.add(new Pat("p2a").birthDate("2002-01-01").gender("male"));
    Reference pat2b = client.add(new Pat("p2b").birthDate("2002-01-01").gender("female"));
    Reference pat2c = client.add(new Pat("p2c").birthDate("1952-01-01").gender("male"));
    Reference pat2d = client.add(new Pat("p2d").birthDate("1952-01-01").gender("female"));
    Reference pat2e = client.add(new Pat("p2e").birthDate("1952-01-01").gender("male"));

    Reference pat3a = client.add(new Pat("p3a").birthDate("2003-01-01").gender("male"));
    Reference pat3b = client.add(new Pat("p3b").birthDate("2003-01-01").gender("female"));
    Reference pat3c = client.add(new Pat("p3c").birthDate("1953-01-01").gender("male"));
    Reference pat3d = client.add(new Pat("p3d").birthDate("1953-01-01").gender("female"));
    Reference pat3e = client.add(new Pat("p3e").birthDate("1953-01-01").gender("male"));

    Reference pat4 = client.add(new Pat("p4").birthDate("1940-01-03").gender("male"));

    Reference med1 = client.add(new Med("m1", "atc1"));
    Reference med2 = client.add(new Med("m2", "atc2"));

    client.add(new MedAdm("p1a-m", pat1a, med1).date("2020-01-01"));
    client.add(new MedAdm("p1b-m", pat1b, med1).date("2020-01-01"));
    client.add(new MedAdm("p1c-m", pat1c, med1).date("2020-01-01"));
    client.add(new MedAdm("p1d-m", pat1d, med1).date("2020-01-01"));
    client.add(new MedAdm("p1e-m", pat1e, med1).date("2019-01-01"));

    client.add(new MedReq("p2a-m", pat2a, med1).date("2020-01-01"));
    client.add(new MedReq("p2b-m", pat2b, med1).date("2020-01-01"));
    client.add(new MedReq("p2c-m", pat2c, med1).date("2020-01-01"));
    client.add(new MedReq("p2d-m", pat2d, med1).date("2020-01-01"));
    client.add(new MedReq("p2e-m", pat2e, med1).date("2019-01-01"));

    client.add(new MedSta("p3a-m", pat3a, med1).date("2020-01-01"));
    client.add(new MedSta("p3b-m", pat3b, med1).date("2020-01-01"));
    client.add(new MedSta("p3c-m", pat3c, med1).date("2020-01-01"));
    client.add(new MedSta("p3d-m", pat3d, med1).date("2020-01-01"));
    client.add(new MedSta("p3e-m", pat3e, med1).date("2019-01-01"));

    client.add(new MedAdm("p4-m", pat4, med2).date("2020-01-04"));

    try {
      ResultSet rs =
          new Que("config/Default_FHIR_Adapter_Test.yml", med, sex, male, age, old)
              .inc(med, Res.ge(DateUtil.parse("2020-01-01")))
              .inc(male)
              .inc(old)
              .execute();
      assertEquals(
          Set.of(pat1c.getReference(), pat2c.getReference(), pat3c.getReference()),
          rs.getSubjectIds());
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
  }
}
