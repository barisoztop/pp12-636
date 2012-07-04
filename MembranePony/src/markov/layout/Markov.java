package markov.layout;

import data.AminoAcid;
import data.Constants;
import data.SSE;
import interfaces.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import markov.graph.*;
import org.apache.log4j.Logger;
import org.jgrapht.ext.GraphMLExporter;

/**
 *
 * @author rgreil
 */
public final class Markov implements Predictor {

	private static final Logger logger = Logger.getLogger(Markov.class);
	private Map<String, Vertex> mapVertex;
	private final MarkovDirectedWeightedGraph<Vertex, Edge> wintermute;
	private Normalizer norm;
	private double hpSteppingValue = 0.1d;
	private double hpRoundingValue = 10d;
	private int hpscaleUsed = -1;
	private static final double HP_MIN = -5.0d;
	private static final double HP_MAX = 6.0d;
	private boolean trained = false;
	private final int middle = (Constants.WINDOW_LENGTH / 2);
	private boolean windowNew = false;
	private double normalizedMin = 0.000001d;

	private enum SpecialVertex {

		TMH,
		NON_TMH,
		//        OUTSIDE,
		//        INSIDE,
		GECONNYSE,
		NULL
	};
	private final Vertex TMH = new Vertex(SpecialVertex.TMH, SpecialVertex.NULL, Double.NaN);
	private final Vertex NON_TMH = new Vertex(SpecialVertex.NON_TMH, SpecialVertex.NULL, Double.NaN);
//    private final Vertex OUTSIDE = new Vertex(SpecialVertex.OUTSIDE, SpecialVertex.NULL, Double.NaN);
//    private final Vertex INSIDE = new Vertex(SpecialVertex.INSIDE, SpecialVertex.NULL, Double.NaN);
	private final Vertex GECONNYSE = new Vertex(SpecialVertex.GECONNYSE, SpecialVertex.NULL, Double.NaN);

	public Markov() {
		logger.info("spawning new " + this.getClass().getSimpleName());
		wintermute = new MarkovDirectedWeightedGraph<Vertex, Edge>(Edge.class);
	}

	protected void addVertices() {
		long start = System.currentTimeMillis();
		mapVertex = new HashMap<String, Vertex>();
		logger.info("creating vertices");
		Vertex[] vArray = new Vertex[]{TMH, NON_TMH, GECONNYSE};
		for (Vertex v : vArray) {
			wintermute.addVertex(v);
//            mapVertex.put(v.getAminoacid() + ":" + v.getSse() + ":" + v.getHydrophobocity(), v);
		}
		//create nodes and add them to the graph
		for (int sse = 0; sse < SSE.values().length; sse++) {
			//sse = the secondary structure of all available at SSE.values()
//            String value_sse = SSE.values()[sse].toString().intern();
			for (int aa = 0; aa < AminoAcid.values().length; aa++) {
				//aa = the aminoacid of all available at AminoAcid.values()
//                String value_aa = AminoAcid.values()[aa].toString().intern();
				double value_hp = HP_MIN;
				while (value_hp < HP_MAX) {
					//hp = the hydrophobocity value from min to max
					Vertex tmp = new Vertex(AminoAcid.values()[aa], SSE.values()[sse], round(value_hp));
					logger.trace("created vertex: " + tmp);
					mapVertex.put(tmp.getAminoacid() + ":" + tmp.getSse() + ":" + tmp.getHydrophobocity(), tmp);
					wintermute.addVertex(tmp);
					value_hp += hpSteppingValue;
				}
			}
		}
		long end = System.currentTimeMillis();
		logger.info("-> " + wintermute.vertexSet().size() + " vertices in " + (end - start) + " ms");
	}

	@Override
	public Prediction predict(Sequence sequence) {
		if (!trained) {
			throw new VerifyError("Can not predict with an empty model! Train it before!");
		}
		checkScale(sequence.getSequence()[0].getHydrophobicityMatrix());
		List<Result> pred = new ArrayList<Result>();
		Result[] predictions = new Result[1];

		//classify
		int counterFalsePredicted = 0;
		for (SlidingWindow slidingWindow : sequence.getWindows()) {
			Vertex vertexMiddle = null;
			SequencePosition spMiddle = null;

			List<Edge> listWindowEdges = new ArrayList<Edge>(Constants.WINDOW_LENGTH);

			for (int i = 0; i < slidingWindow.getSequence().length - 1; i++) {
				SequencePosition spSource = slidingWindow.getSequence()[i];
				Vertex vertexSource = null;
				SequencePosition spTarget = slidingWindow.getSequence()[i + 1];
				Vertex vertexTarget = null;

				//source
				if (spSource == null) {
					continue;
				} else {
					String sourceAa = spSource.getAminoAcid().toString().intern();
					String sourceSse = spSource.getSecondaryStructure().toString().intern();
					Double sourceHp = round(spSource.getHydrophobicity());
					vertexSource = mapVertex.get(sourceAa + ":" + sourceSse + ":" + sourceHp);
				}

				if (i == middle) {
					vertexMiddle = vertexSource;
					spMiddle = spSource;
				}

				//target
				if (spTarget == null) {
					continue;
				} else {
					String targetAa = spTarget.getAminoAcid().toString().intern();
					String targetSse = spTarget.getSecondaryStructure().toString().intern();
					Double targetHp = round(spTarget.getHydrophobicity());
					vertexTarget = mapVertex.get(targetAa + ":" + targetSse + ":" + targetHp);
				}

				listWindowEdges.add(wintermute.getEdge(vertexSource, vertexTarget));
			}

			{
				//classification
				double weightWindowSumTmh = 0d;
				double weightWindowSumNonTmh = 0d;
				double weightWindowSumBoth = 0d;
				double weightWindowSumRatioTmh = 0d;
				double weightWindowSumRatioNonTmh = 0d;

				for (Edge edge : listWindowEdges) {
					if (edge != null) {
						//calculate ratio of weight vs. corresponding tmh/nonTmh weight
						double weightEdgeRatioTmh = (edge.getWeightTmh() / edge.getWeight());
						double weightEdgeRatioNonTmh = (edge.getWeightNonTmh() / edge.getWeight());

						if (weightEdgeRatioTmh == 0d) {
							weightEdgeRatioTmh = normalizedMin;
						}
						if (weightEdgeRatioNonTmh == 0d) {
							weightEdgeRatioNonTmh = normalizedMin;
						}

						//sum up all ratios
						weightWindowSumRatioTmh += weightEdgeRatioTmh;
						weightWindowSumRatioNonTmh += weightEdgeRatioNonTmh;

						//naive bayes (modified)
						weightWindowSumTmh += edge.getWeightTmh();
						weightWindowSumNonTmh += edge.getWeightNonTmh();
						weightWindowSumBoth += edge.getWeight();
					}
				}

				//naive bayes (modified)
				double classRateTmh = weightWindowSumTmh / weightWindowSumBoth;
				double classRateNonTmh = weightWindowSumNonTmh / weightWindowSumBoth;

				Edge edgeTmh = wintermute.getEdge(vertexMiddle, TMH);
				double weightTmh = weightWindowSumRatioTmh * classRateTmh;
				if (edgeTmh == null) {
					weightTmh *= normalizedMin;
				} else {
					weightTmh *= edgeTmh.getWeight();
				}

				Edge edgeNonTmh = wintermute.getEdge(vertexMiddle, NON_TMH);
				double weightNonTmh = weightWindowSumRatioNonTmh * classRateNonTmh;
				if (edgeNonTmh == null) {
					weightNonTmh *= normalizedMin;
				} else {
					weightNonTmh *= edgeNonTmh.getWeight();
				}

				Result predicted = null;
				if (weightTmh > weightNonTmh) {
					predicted = Result.TMH;
				} else if (weightTmh < weightNonTmh) {
					predicted = Result.NON_TMH;
				} else {
					logger.fatal("WARNING: probability for TMH (" + weightTmh + ") and NON_TMH (" + weightNonTmh + ") are equal. Prediction set to: " + Result.OUTSIDE);
					predicted = Result.OUTSIDE;
				}

				if (spMiddle.getRealClass() != predicted) {
					counterFalsePredicted++;
//
//                System.out.println("MIDDLE: " + vertexMiddle);
//                System.out.println("\tweightWindowTmh: " + weightWindowTmh);
//                System.out.println("\tweightWindowNonTmh: " + weightWindowNonTmh);
//                System.out.println("\tCR:Tmh: " + classRateTmh);
//                System.out.println("\tCR:NonTmh: " + classRateNonTmh);
//                System.out.println("\t-> edge:Tmh: " + edgeTmh);
//                System.out.println("\t-> edge:NonTmh: " + edgeNonTmh);
//                System.out.println("\t--> probability");
//                System.out.println("\t--> TMH: " + weightTmh);
//                System.out.println("\t--> NON TMH: " + weightNonTmh);
//                System.out.println("\t---> RESULT");
//                System.out.println("\t---> REAL: " + spMiddle.getRealClass());
//                System.out.println("\t---> PRED: " + predicted);
				}
				pred.add(predicted);
			}
		}
		predictions = pred.toArray(predictions);
		logger.debug("FALSE PREDICTION: " + counterFalsePredicted + " (" + ((int) (100d / (double) sequence.length() * counterFalsePredicted)) + "%) (id: " + sequence.getId() + " -> length: " + sequence.length() + ")");
		return new GenericPrediction(sequence, predictions);
	}

	@Override
	public void train(Sequence[] trainingCases) {
		if (trained) {
			throw new VerifyError("Model can not be overtrained! Create new empty Instance!");
		}
		addVertices();
		long start = System.currentTimeMillis();
		logger.info("training " + trainingCases.length + " sequences");
		checkScale(trainingCases[0].getSequence()[0].getHydrophobicityMatrix());

		HashMap<String, Boolean> MAP_SOLUBLE = new HashMap<String, Boolean>();
		MAP_SOLUBLE.put("1433S_HUMAN", true);
		MAP_SOLUBLE.put("3HAO_YEAST", true);
		MAP_SOLUBLE.put("A1AHH2_ECOK1", true);
		MAP_SOLUBLE.put("A41_VACCW", true);
		MAP_SOLUBLE.put("A5A5U1_9INFA", true);
		MAP_SOLUBLE.put("ACO13_MOUSE", true);
		MAP_SOLUBLE.put("ACPS_STAAC", true);
		MAP_SOLUBLE.put("ACP_BORBU", true);
		MAP_SOLUBLE.put("ADHX_HUMAN", true);
		MAP_SOLUBLE.put("AHSP_HUMAN", true);
		MAP_SOLUBLE.put("ALAXS_PYRHO", true);
		MAP_SOLUBLE.put("ALB2_LATSA", true);
		MAP_SOLUBLE.put("ALBA_PYRHO", true);
		MAP_SOLUBLE.put("ALD2_MOUSE", true);
		MAP_SOLUBLE.put("ALF1_THETE", true);
		MAP_SOLUBLE.put("ALL7_DERPT", true);
		MAP_SOLUBLE.put("AMCI_APIME", true);
		MAP_SOLUBLE.put("AMPS2_RANPI", true);
		MAP_SOLUBLE.put("ANP1_RHIDE", true);
		MAP_SOLUBLE.put("ANTA_HAEOF", true);
		MAP_SOLUBLE.put("APC10_YEAST", true);
		MAP_SOLUBLE.put("API3_ASCSU", true);
		MAP_SOLUBLE.put("APL3_MANSE", true);
		MAP_SOLUBLE.put("APOM_HUMAN", true);
		MAP_SOLUBLE.put("APT_HUMAN", true);
		MAP_SOLUBLE.put("AR2BP_HUMAN", true);
		MAP_SOLUBLE.put("ARC1B_BOVIN", true);
		MAP_SOLUBLE.put("ARGR1_ARGRF", true);
		MAP_SOLUBLE.put("ARGR_BACSU", true);
		MAP_SOLUBLE.put("ARK72_MOUSE", true);
		MAP_SOLUBLE.put("ARL2_HUMAN", true);
		MAP_SOLUBLE.put("ARPC3_BOVIN", true);
		MAP_SOLUBLE.put("ARPC4_SCHPO", true);
		MAP_SOLUBLE.put("ARPC5_BOVIN", true);
		MAP_SOLUBLE.put("ASPG_WOLSU", true);
		MAP_SOLUBLE.put("AT12B_ARATH", true);
		MAP_SOLUBLE.put("ATX1_YEAST", true);
		MAP_SOLUBLE.put("B2LA1_MOUSE", true);
		MAP_SOLUBLE.put("B2MG_MOUSE", true);
		MAP_SOLUBLE.put("BAF_HUMAN", true);
		MAP_SOLUBLE.put("BARF1_EBVB9", true);
		MAP_SOLUBLE.put("BARS_BACAM", true);
		MAP_SOLUBLE.put("BDEL_HIRME", true);
		MAP_SOLUBLE.put("BEV1A_BETPN", true);
		MAP_SOLUBLE.put("BIOA_ECOLI", true);
		MAP_SOLUBLE.put("BIOD_HELPY", true);
		MAP_SOLUBLE.put("BIRC5_MOUSE", true);
		MAP_SOLUBLE.put("BLAI_MYCTU", true);
		MAP_SOLUBLE.put("BPT1_BOVIN", true);
		MAP_SOLUBLE.put("BRK1_HUMAN", true);
		MAP_SOLUBLE.put("BTUR_SALTY", true);
		MAP_SOLUBLE.put("BUBL_PENBR", true);
		MAP_SOLUBLE.put("BUCA_BUNCA", true);
		MAP_SOLUBLE.put("B_BPPHX", true);
		MAP_SOLUBLE.put("CADF1_PLAFX", true);
		MAP_SOLUBLE.put("CAF1_SCHPO", true);
		MAP_SOLUBLE.put("CAH7_HUMAN", true);
		MAP_SOLUBLE.put("CAN_YEAST", true);
		MAP_SOLUBLE.put("CAPSD_FHV", true);
		MAP_SOLUBLE.put("CATA_HELPY", true);
		MAP_SOLUBLE.put("CAZA1_CHICK", true);
		MAP_SOLUBLE.put("CBB2_CARML", true);
		MAP_SOLUBLE.put("CBPB1_HUMAN", true);
		MAP_SOLUBLE.put("CCLA_CARML", true);
		MAP_SOLUBLE.put("CDC31_YEAST", true);
		MAP_SOLUBLE.put("CEPL_CERFI", true);
		MAP_SOLUBLE.put("CEST_ECO57", true);
		MAP_SOLUBLE.put("CGHB_HUMAN", true);
		MAP_SOLUBLE.put("CH10_ECOLI", true);
		MAP_SOLUBLE.put("CH3L1_SHEEP", true);
		MAP_SOLUBLE.put("CHEB_SALTY", true);
		MAP_SOLUBLE.put("CHEW_ECOLI", true);
		MAP_SOLUBLE.put("CHEW_THEMA", true);
		MAP_SOLUBLE.put("CHIS_STRSN", true);
		MAP_SOLUBLE.put("CHMU_BACSU", true);
		MAP_SOLUBLE.put("CHP1_HUMAN", true);
		MAP_SOLUBLE.put("CHSP1_HUMAN", true);
		MAP_SOLUBLE.put("CLPP_STRR6", true);
		MAP_SOLUBLE.put("CLSS_HAEMA", true);
		MAP_SOLUBLE.put("CMAS3_MYCTU", true);
		MAP_SOLUBLE.put("CNBP1_HUMAN", true);
		MAP_SOLUBLE.put("CO5_PIG", true);
		MAP_SOLUBLE.put("CO8G_HUMAN", true);
		MAP_SOLUBLE.put("COAD_ARCFU", true);
		MAP_SOLUBLE.put("COAD_BACSU", true);
		MAP_SOLUBLE.put("COAE_HAEIN", true);
		MAP_SOLUBLE.put("COAG_TACTR", true);
		MAP_SOLUBLE.put("COAX_CAMJE", true);
		MAP_SOLUBLE.put("COFI_SCHPO", true);
		MAP_SOLUBLE.put("COPZ_ENTHR", true);
		MAP_SOLUBLE.put("CPD_ARATH", true);
		MAP_SOLUBLE.put("CREN7_SULSO", true);
		MAP_SOLUBLE.put("CRVP1_NAJAT", true);
		MAP_SOLUBLE.put("CRYL1_HUMAN", true);
		MAP_SOLUBLE.put("CSF2_HUMAN", true);
		MAP_SOLUBLE.put("CSH_HUMAN", true);
		MAP_SOLUBLE.put("CSOR_MYCTU", true);
		MAP_SOLUBLE.put("CSPB_BACSU", true);
		MAP_SOLUBLE.put("CSRP1_CHICK", true);
		MAP_SOLUBLE.put("CTRA_BOVIN", true);
		MAP_SOLUBLE.put("CUER_ECOLI", true);
		MAP_SOLUBLE.put("CUTA_THEMA", true);
		MAP_SOLUBLE.put("CUTC_SHIFL", true);
		MAP_SOLUBLE.put("CX1_NAJPA", true);
		MAP_SOLUBLE.put("CYTB_HUMAN", true);
		MAP_SOLUBLE.put("CYTD_HUMAN", true);
		MAP_SOLUBLE.put("D0VWZ2_EUBE2", true);
		MAP_SOLUBLE.put("DACX_STRSK", true);
		MAP_SOLUBLE.put("DAPK2_MOUSE", true);
		MAP_SOLUBLE.put("DEF_PENBA", true);
		MAP_SOLUBLE.put("DIS1A_DICDI", true);
		MAP_SOLUBLE.put("DIST_TRIFL", true);
		MAP_SOLUBLE.put("DLRB1_HUMAN", true);
		MAP_SOLUBLE.put("DPH3_HUMAN", true);
		MAP_SOLUBLE.put("DPO3B_ECOLI", true);
		MAP_SOLUBLE.put("DPS_ECOLI", true);
		MAP_SOLUBLE.put("DPS_HALSA", true);
		MAP_SOLUBLE.put("DPS_SULSO", true);
		MAP_SOLUBLE.put("DRNE_VIBCH", true);
		MAP_SOLUBLE.put("DRTI_DELRE", true);
		MAP_SOLUBLE.put("DSVC_DESVH", true);
		MAP_SOLUBLE.put("DTD_AQUAE", true);
		MAP_SOLUBLE.put("DTD_LEIMA", true);
		MAP_SOLUBLE.put("DUPD1_HUMAN", true);
		MAP_SOLUBLE.put("DYL2_HUMAN", true);
		MAP_SOLUBLE.put("DYLT_DROME", true);
		MAP_SOLUBLE.put("D_BPPHX", true);
		MAP_SOLUBLE.put("E6PBR9_TETTH", true);
		MAP_SOLUBLE.put("ECM15_YEAST", true);
		MAP_SOLUBLE.put("ECR1_AERPE", true);
		MAP_SOLUBLE.put("EFP_PSEAE", true);
		MAP_SOLUBLE.put("EIF3K_HUMAN", true);
		MAP_SOLUBLE.put("ELIB_PHYCI", true);
		MAP_SOLUBLE.put("ELOC_YEAST", true);
		MAP_SOLUBLE.put("ENOPH_YEAST", true);
		MAP_SOLUBLE.put("ESXA_MYCTU", true);
		MAP_SOLUBLE.put("ESXA_STAAM", true);
		MAP_SOLUBLE.put("ESXB_MYCTU", true);
		MAP_SOLUBLE.put("EX7S_BORPA", true);
		MAP_SOLUBLE.put("EXOS1_HUMAN", true);
		MAP_SOLUBLE.put("EXOS4_HUMAN", true);
		MAP_SOLUBLE.put("EXOS7_HUMAN", true);
		MAP_SOLUBLE.put("FABA_ECOLI", true);
		MAP_SOLUBLE.put("FABH_STAAW", true);
		MAP_SOLUBLE.put("FABP5_HUMAN", true);
		MAP_SOLUBLE.put("FABPI_HUMAN", true);
		MAP_SOLUBLE.put("FABPL_HUMAN", true);
		MAP_SOLUBLE.put("FGF1_HUMAN", true);
		MAP_SOLUBLE.put("FHIT_HUMAN", true);
		MAP_SOLUBLE.put("FKB1A_HUMAN", true);
		MAP_SOLUBLE.put("FLHC_ECOLI", true);
		MAP_SOLUBLE.put("FLHD_ECOLI", true);
		MAP_SOLUBLE.put("FLIS_BACSU", true);
		MAP_SOLUBLE.put("FLIT_YERE8", true);
		MAP_SOLUBLE.put("FLSO_ASFB7", true);
		MAP_SOLUBLE.put("FMNB_DESVM", true);
		MAP_SOLUBLE.put("FOSA_SERMA", true);
		MAP_SOLUBLE.put("FRMSR_YEAST", true);
		MAP_SOLUBLE.put("FTN_HELPJ", true);
		MAP_SOLUBLE.put("FUCL_ANGAN", true);
		MAP_SOLUBLE.put("FUCM_ECOLI", true);
		MAP_SOLUBLE.put("FUR_HELPY", true);
		MAP_SOLUBLE.put("G3P1_KLULA", true);
		MAP_SOLUBLE.put("G6PI_PYRFU", true);
		MAP_SOLUBLE.put("GCH1_RAT", true);
		MAP_SOLUBLE.put("GCTB_ACIFV", true);
		MAP_SOLUBLE.put("GDIR1_BOVIN", true);
		MAP_SOLUBLE.put("GGGPS_ARCFU", true);
		MAP_SOLUBLE.put("GLB2_PHAPT", true);
		MAP_SOLUBLE.put("GLB3_LUMTE", true);
		MAP_SOLUBLE.put("GLBH_CAEEL", true);
		MAP_SOLUBLE.put("GLNA1_MYCTU", true);
		MAP_SOLUBLE.put("GLOX_BACSU", true);
		MAP_SOLUBLE.put("GLPE_ECOLI", true);
		MAP_SOLUBLE.put("GLRX1_PIG", true);
		MAP_SOLUBLE.put("GLRX4_ECOLI", true);
		MAP_SOLUBLE.put("GLTP_BOVIN", true);
		MAP_SOLUBLE.put("GPX4_HUMAN", true);
		MAP_SOLUBLE.put("GS13_BACSU", true);
		MAP_SOLUBLE.put("GSKIP_HUMAN", true);
		MAP_SOLUBLE.put("GSTM7_MOUSE", true);
		MAP_SOLUBLE.put("GUN7_TRIRE", true);
		MAP_SOLUBLE.put("G_BPG4", true);
		MAP_SOLUBLE.put("HASA_SERMA", true);
		MAP_SOLUBLE.put("HBP2_RHIAP", true);
		MAP_SOLUBLE.put("HBXIP_HUMAN", true);
		MAP_SOLUBLE.put("HCP1_PSEAE", true);
		MAP_SOLUBLE.put("HCY2E_RAPVE", true);
		MAP_SOLUBLE.put("HEBP1_MOUSE", true);
		MAP_SOLUBLE.put("HER1_CAEEL", true);
		MAP_SOLUBLE.put("HINT1_RABIT", true);
		MAP_SOLUBLE.put("HIRV2_HIRME", true);
		MAP_SOLUBLE.put("HIS1_LACLA", true);
		MAP_SOLUBLE.put("HIS2_CHRVO", true);
		MAP_SOLUBLE.put("HIS2_MYCTU", true);
		MAP_SOLUBLE.put("HIS3_METTH", true);
		MAP_SOLUBLE.put("HLA_STAAU", true);
		MAP_SOLUBLE.put("HMFA_METFE", true);
		MAP_SOLUBLE.put("HMP_ECOLI", true);
		MAP_SOLUBLE.put("HOP_MOUSE", true);
		MAP_SOLUBLE.put("HPRT_ECOLI", true);
		MAP_SOLUBLE.put("HS16B_WHEAT", true);
		MAP_SOLUBLE.put("HSLV_THEMA", true);
		MAP_SOLUBLE.put("HSPQ_ECOLI", true);
		MAP_SOLUBLE.put("HSPS_METJA", true);
		MAP_SOLUBLE.put("HUGAA_VESVU", true);
		MAP_SOLUBLE.put("I22R2_HUMAN", true);
		MAP_SOLUBLE.put("IAA1_WHEAT", true);
		MAP_SOLUBLE.put("IAAT_ELECO", true);
		MAP_SOLUBLE.put("ICAA_ASCSU", true);
		MAP_SOLUBLE.put("ICE1_ASCSU", true);
		MAP_SOLUBLE.put("IDI_SALTY", true);
		MAP_SOLUBLE.put("IF1_ECOLI", true);
		MAP_SOLUBLE.put("IF5A1_YEAST", true);
		MAP_SOLUBLE.put("IFNG_RABIT", true);
		MAP_SOLUBLE.put("IL10_HUMAN", true);
		MAP_SOLUBLE.put("IL12A_HUMAN", true);
		MAP_SOLUBLE.put("IL13_HUMAN", true);
		MAP_SOLUBLE.put("IL17_HUMAN", true);
		MAP_SOLUBLE.put("IL1RA_HUMAN", true);
		MAP_SOLUBLE.put("IL21_HUMAN", true);
		MAP_SOLUBLE.put("IL23A_HUMAN", true);
		MAP_SOLUBLE.put("IL2_HUMAN", true);
		MAP_SOLUBLE.put("IL7_HUMAN", true);
		MAP_SOLUBLE.put("IMPA1_BOVIN", true);
		MAP_SOLUBLE.put("IOVO_CROCS", true);
		MAP_SOLUBLE.put("ISDG_STAAN", true);
		MAP_SOLUBLE.put("ISG15_HUMAN", true);
		MAP_SOLUBLE.put("ISH1_STOHE", true);
		MAP_SOLUBLE.put("ISK1_PIG", true);
		MAP_SOLUBLE.put("ITPA_HUMAN", true);
		MAP_SOLUBLE.put("ITRP_HALRO", true);
		MAP_SOLUBLE.put("IVBII_DENPO", true);
		MAP_SOLUBLE.put("KADA_METTL", true);
		MAP_SOLUBLE.put("KAD_MYCTU", true);
		MAP_SOLUBLE.put("KCRM_TORCA", true);
		MAP_SOLUBLE.put("KDSA_VIBC3", true);
		MAP_SOLUBLE.put("KGUA_ANAPZ", true);
		MAP_SOLUBLE.put("KIF5C_RAT", true);
		MAP_SOLUBLE.put("KITH_UREPA", true);
		MAP_SOLUBLE.put("KPSU5_ECOLX", true);
		MAP_SOLUBLE.put("LACB1_HORSE", true);
		MAP_SOLUBLE.put("LAPP_HAEOF", true);
		MAP_SOLUBLE.put("LEG1_BUFAR", true);
		MAP_SOLUBLE.put("LEP_HUMAN", true);
		MAP_SOLUBLE.put("LKHA4_YEAST", true);
		MAP_SOLUBLE.put("LOG3_ARATH", true);
		MAP_SOLUBLE.put("LOT6_YEAST", true);
		MAP_SOLUBLE.put("LPLAN_THEAC", true);
		MAP_SOLUBLE.put("LSM3_YEAST", true);
		MAP_SOLUBLE.put("LSRG_YERPE", true);
		MAP_SOLUBLE.put("LXN_HUMAN", true);
		MAP_SOLUBLE.put("LY86_MOUSE", true);
		MAP_SOLUBLE.put("LY96_HUMAN", true);
		MAP_SOLUBLE.put("LYG_CYGAT", true);
		MAP_SOLUBLE.put("LYSC_NUMME", true);
		MAP_SOLUBLE.put("LYS_MERLU", true);
		MAP_SOLUBLE.put("M4GDB_DANRE", true);
		MAP_SOLUBLE.put("MAAI_MOUSE", true);
		MAP_SOLUBLE.put("MAF_BACSU", true);
		MAP_SOLUBLE.put("MAMB_DENJA", true);
		MAP_SOLUBLE.put("MATRX_BDV", true);
		MAP_SOLUBLE.put("MCA3_HUMAN", true);
		MAP_SOLUBLE.put("MCTS1_HUMAN", true);
		MAP_SOLUBLE.put("MD2BP_HUMAN", true);
		MAP_SOLUBLE.put("MD2L2_HUMAN", true);
		MAP_SOLUBLE.put("MECI_STAAN", true);
		MAP_SOLUBLE.put("MEN2_EUPNO", true);
		MAP_SOLUBLE.put("MER23_EUPRA", true);
		MAP_SOLUBLE.put("META_BACC1", true);
		MAP_SOLUBLE.put("METJ_ECOLI", true);
		MAP_SOLUBLE.put("MEX1_JACME", true);
		MAP_SOLUBLE.put("MGN_DROME", true);
		MAP_SOLUBLE.put("MGSA_ECOLI", true);
		MAP_SOLUBLE.put("MIF_HUMAN", true);
		MAP_SOLUBLE.put("MIOX_HUMAN", true);
		MAP_SOLUBLE.put("MIT1_DENPO", true);
		MAP_SOLUBLE.put("MNTR_BACSU", true);
		MAP_SOLUBLE.put("MOBA_ECOLI", true);
		MAP_SOLUBLE.put("MPGS_PYRHO", true);
		MAP_SOLUBLE.put("MSP1_ASCSU", true);
		MAP_SOLUBLE.put("MSRB1_HUMAN", true);
		MAP_SOLUBLE.put("MTAP_HUMAN", true);
		MAP_SOLUBLE.put("MTHFS_MYCPN", true);
		MAP_SOLUBLE.put("MTPN_MOUSE", true);
		MAP_SOLUBLE.put("MUG_ECOLI", true);
		MAP_SOLUBLE.put("MUTH_HAEIN", true);
		MAP_SOLUBLE.put("MVL_MICVR", true);
		MAP_SOLUBLE.put("NAA50_HUMAN", true);
		MAP_SOLUBLE.put("NADM_METJA", true);
		MAP_SOLUBLE.put("NANA_HAEIN", true);
		MAP_SOLUBLE.put("NAPD_ECOLI", true);
		MAP_SOLUBLE.put("NCBP2_HUMAN", true);
		MAP_SOLUBLE.put("NDK_BACAN", true);
		MAP_SOLUBLE.put("NEUA_NEIME", true);
		MAP_SOLUBLE.put("NGAL_HUMAN", true);
		MAP_SOLUBLE.put("NGF_MOUSE", true);
		MAP_SOLUBLE.put("NIRD_ECOLI", true);
		MAP_SOLUBLE.put("NNMT_MOUSE", true);
		MAP_SOLUBLE.put("NPC2_BOVIN", true);
		MAP_SOLUBLE.put("NQO1_RAT", true);
		MAP_SOLUBLE.put("NT5C_HUMAN", true);
		MAP_SOLUBLE.put("NTAQ1_HUMAN", true);
		MAP_SOLUBLE.put("NTF2_HUMAN", true);
		MAP_SOLUBLE.put("NUDT3_HUMAN", true);
		MAP_SOLUBLE.put("NXB4_CERLA", true);
		MAP_SOLUBLE.put("NXL1_NAJOX", true);
		MAP_SOLUBLE.put("OBP_BOVIN", true);
		MAP_SOLUBLE.put("ODHI_CORGL", true);
		MAP_SOLUBLE.put("OGT_METJA", true);
		MAP_SOLUBLE.put("OMP_MOUSE", true);
		MAP_SOLUBLE.put("ORN_XANCP", true);
		MAP_SOLUBLE.put("OSMC_ECOLI", true);
		MAP_SOLUBLE.put("OTC_GLOVI", true);
		MAP_SOLUBLE.put("P21_BYVU", true);
		MAP_SOLUBLE.put("P72393_STRCO", true);
		MAP_SOLUBLE.put("P8_RDVO", true);
		MAP_SOLUBLE.put("PA1L_PSEAE", true);
		MAP_SOLUBLE.put("PA21B_BOTPI", true);
		MAP_SOLUBLE.put("PAI2_HUMAN", true);
		MAP_SOLUBLE.put("PANB_NEIMB", true);
		MAP_SOLUBLE.put("PANC_THEMA", true);
		MAP_SOLUBLE.put("PAND_FRATT", true);
		MAP_SOLUBLE.put("PAPK_ECOLX", true);
		MAP_SOLUBLE.put("PCP_PYRHO", true);
		MAP_SOLUBLE.put("PDUO_BACSU", true);
		MAP_SOLUBLE.put("PDXJ_BURP1", true);
		MAP_SOLUBLE.put("PEA15_CRIGR", true);
		MAP_SOLUBLE.put("PELO_SULSO", true);
		MAP_SOLUBLE.put("PER1_ARAHY", true);
		MAP_SOLUBLE.put("PERR_BACSU", true);
		MAP_SOLUBLE.put("PFDA_METTH", true);
		MAP_SOLUBLE.put("PFDA_PYRHO", true);
		MAP_SOLUBLE.put("PFDB_METTH", true);
		MAP_SOLUBLE.put("PGLR2_PECCC", true);
		MAP_SOLUBLE.put("PGPSA_DROME", true);
		MAP_SOLUBLE.put("PHO80_YEAST", true);
		MAP_SOLUBLE.put("PHOS_BOVIN", true);
		MAP_SOLUBLE.put("PHOU_STRPN", true);
		MAP_SOLUBLE.put("PHS_RAT", true);
		MAP_SOLUBLE.put("PIMT_DROME", true);
		MAP_SOLUBLE.put("PIPNA_HUMAN", true);
		MAP_SOLUBLE.put("PLY_BACSU", true);
		MAP_SOLUBLE.put("PNC1_YEAST", true);
		MAP_SOLUBLE.put("PNG1_YEAST", true);
		MAP_SOLUBLE.put("PPA5_PIG", true);
		MAP_SOLUBLE.put("PPAC_HUMAN", true);
		MAP_SOLUBLE.put("PPAF_PHAVU", true);
		MAP_SOLUBLE.put("PPCT_HUMAN", true);
		MAP_SOLUBLE.put("PPIA_MACMU", true);
		MAP_SOLUBLE.put("PPIC_ECOLI", true);
		MAP_SOLUBLE.put("PPTA_ECOLI", true);
		MAP_SOLUBLE.put("PRDX2_HUMAN", true);
		MAP_SOLUBLE.put("PRO1B_ACACA", true);
		MAP_SOLUBLE.put("PROB_CAMJE", true);
		MAP_SOLUBLE.put("PROF2_MOUSE", true);
		MAP_SOLUBLE.put("PROF_PLAFX", true);
		MAP_SOLUBLE.put("PRSF_ECOLX", true);
		MAP_SOLUBLE.put("PSA2_BOVIN", true);
		MAP_SOLUBLE.put("PSA_MYCTU", true);
		MAP_SOLUBLE.put("PSCG_PSEAE", true);
		MAP_SOLUBLE.put("PTFB1_ECOLI", true);
		MAP_SOLUBLE.put("PTHP_STAAU", true);
		MAP_SOLUBLE.put("PTH_ECOLI", true);
		MAP_SOLUBLE.put("PTH_SULSO", true);
		MAP_SOLUBLE.put("PTKB_ECOL6", true);
		MAP_SOLUBLE.put("PTMA_ECOLI", true);
		MAP_SOLUBLE.put("PTQA_ECOLI", true);
		MAP_SOLUBLE.put("PTQB_ECOLI", true);
		MAP_SOLUBLE.put("PTSN_ECOLI", true);
		MAP_SOLUBLE.put("PYDC1_HUMAN", true);
		MAP_SOLUBLE.put("PYL2_ARATH", true);
		MAP_SOLUBLE.put("PYRD_TRYCR", true);
		MAP_SOLUBLE.put("PYRK_LACLM", true);
		MAP_SOLUBLE.put("Q0PBC3_CAMJE", true);
		MAP_SOLUBLE.put("Q2GFI3_EHRCR", true);
		MAP_SOLUBLE.put("Q5G940_HELPY", true);
		MAP_SOLUBLE.put("Q63XL8_BURPS", true);
		MAP_SOLUBLE.put("Q7WG29_BORBR", true);
		MAP_SOLUBLE.put("Q9A097_STRP1", true);
		MAP_SOLUBLE.put("QUEF_VIBCH", true);
		MAP_SOLUBLE.put("RANSM_POLLE", true);
		MAP_SOLUBLE.put("RAN_HUMAN", true);
		MAP_SOLUBLE.put("RBFA_HAEIN", true);
		MAP_SOLUBLE.put("RBFA_MYCPN", true);
		MAP_SOLUBLE.put("RBM8A_DROME", true);
		MAP_SOLUBLE.put("RBSD_STAA8", true);
		MAP_SOLUBLE.put("RBX1_HUMAN", true);
		MAP_SOLUBLE.put("RECU_BACSU", true);
		MAP_SOLUBLE.put("RECX_ECO57", true);
		MAP_SOLUBLE.put("REE1_YEAST", true);
		MAP_SOLUBLE.put("REG1A_HUMAN", true);
		MAP_SOLUBLE.put("RET4_CHICK", true);
		MAP_SOLUBLE.put("RETN_MOUSE", true);
		MAP_SOLUBLE.put("REV_HV1B1", true);
		MAP_SOLUBLE.put("REX_BACSU", true);
		MAP_SOLUBLE.put("RGN_HUMAN", true);
		MAP_SOLUBLE.put("RHAM_RHILT", true);
		MAP_SOLUBLE.put("RIFK_HUMAN", true);
		MAP_SOLUBLE.put("RIMM_HAEIN", true);
		MAP_SOLUBLE.put("RIMP_STRPN", true);
		MAP_SOLUBLE.put("RIR2B_HUMAN", true);
		MAP_SOLUBLE.put("RL12A_YEAST", true);
		MAP_SOLUBLE.put("RL14A_YEAST", true);
		MAP_SOLUBLE.put("RL15A_YEAST", true);
		MAP_SOLUBLE.put("RL17A_YEAST", true);
		MAP_SOLUBLE.put("RL18_CANFA", true);
		MAP_SOLUBLE.put("RL22A_YEAST", true);
		MAP_SOLUBLE.put("RL23A_YEAST", true);
		MAP_SOLUBLE.put("RL24A_YEAST", true);
		MAP_SOLUBLE.put("RL25_YEAST", true);
		MAP_SOLUBLE.put("RL28_YEAST", true);
		MAP_SOLUBLE.put("RL29_YEAST", true);
		MAP_SOLUBLE.put("RL30_YEAST", true);
		MAP_SOLUBLE.put("RL31A_YEAST", true);
		MAP_SOLUBLE.put("RL32_YEAST", true);
		MAP_SOLUBLE.put("RL33A_YEAST", true);
		MAP_SOLUBLE.put("RL34A_YEAST", true);
		MAP_SOLUBLE.put("RL35A_YEAST", true);
		MAP_SOLUBLE.put("RL36A_YEAST", true);
		MAP_SOLUBLE.put("RL37A_YEAST", true);
		MAP_SOLUBLE.put("RL38_YEAST", true);
		MAP_SOLUBLE.put("RL39_YEAST", true);
		MAP_SOLUBLE.put("RL43A_YEAST", true);
		MAP_SOLUBLE.put("RL44A_YEAST", true);
		MAP_SOLUBLE.put("RL4B_YEAST", true);
		MAP_SOLUBLE.put("RL6A_YEAST", true);
		MAP_SOLUBLE.put("RL7A_YEAST", true);
		MAP_SOLUBLE.put("RLA1_YEAST", true);
		MAP_SOLUBLE.put("RLA2_YEAST", true);
		MAP_SOLUBLE.put("RLME_ECOLI", true);
		MAP_SOLUBLE.put("RLMH_STAAU", true);
		MAP_SOLUBLE.put("RNH2_PYRKO", true);
		MAP_SOLUBLE.put("RNH_SHEON", true);
		MAP_SOLUBLE.put("RNS11_NICAL", true);
		MAP_SOLUBLE.put("RNSA_STRAU", true);
		MAP_SOLUBLE.put("RPAB2_YEAST", true);
		MAP_SOLUBLE.put("RPB4_YEAST", true);
		MAP_SOLUBLE.put("RPB7_YEAST", true);
		MAP_SOLUBLE.put("RRAA_ECOLI", true);
		MAP_SOLUBLE.put("RRF_AQUAE", true);
		MAP_SOLUBLE.put("RRMF_HAEIN", true);
		MAP_SOLUBLE.put("RS11A_YEAST", true);
		MAP_SOLUBLE.put("RS13_YEAST", true);
		MAP_SOLUBLE.put("RS14A_YEAST", true);
		MAP_SOLUBLE.put("RS15_YEAST", true);
		MAP_SOLUBLE.put("RS16A_YEAST", true);
		MAP_SOLUBLE.put("RS18_CANFA", true);
		MAP_SOLUBLE.put("RS19A_YEAST", true);
		MAP_SOLUBLE.put("RS21A_YEAST", true);
		MAP_SOLUBLE.put("RS23A_YEAST", true);
		MAP_SOLUBLE.put("RS24A_YEAST", true);
		MAP_SOLUBLE.put("RS25A_YEAST", true);
		MAP_SOLUBLE.put("RS28A_YEAST", true);
		MAP_SOLUBLE.put("RS29A_YEAST", true);
		MAP_SOLUBLE.put("RS30A_YEAST", true);
		MAP_SOLUBLE.put("RS3A_TETTH", true);
		MAP_SOLUBLE.put("RS3_YEAST", true);
		MAP_SOLUBLE.put("RS9A_YEAST", true);
		MAP_SOLUBLE.put("RSD_ECOLI", true);
		MAP_SOLUBLE.put("RSMG_ECOLI", true);
		MAP_SOLUBLE.put("RTPA_BACSU", true);
		MAP_SOLUBLE.put("RUBR_DESGI", true);
		MAP_SOLUBLE.put("RUBY_DESVH", true);
		MAP_SOLUBLE.put("RUVX_ECOLI", true);
		MAP_SOLUBLE.put("S100B_RAT", true);
		MAP_SOLUBLE.put("S10A7_HUMAN", true);
		MAP_SOLUBLE.put("S10AG_HUMAN", true);
		MAP_SOLUBLE.put("SAP18_HUMAN", true);
		MAP_SOLUBLE.put("SARR_STAA8", true);
		MAP_SOLUBLE.put("SARS_STAA8", true);
		MAP_SOLUBLE.put("SAT1_HUMAN", true);
		MAP_SOLUBLE.put("SAV_STRAV", true);
		MAP_SOLUBLE.put("SBMC_ECOLI", true);
		MAP_SOLUBLE.put("SCX12_CENNO", true);
		MAP_SOLUBLE.put("SCXV_CENSC", true);
		MAP_SOLUBLE.put("SDF2_ARATH", true);
		MAP_SOLUBLE.put("SDHL_HUMAN", true);
		MAP_SOLUBLE.put("SDO1L_YEAST", true);
		MAP_SOLUBLE.put("SDOS_HUMAN", true);
		MAP_SOLUBLE.put("SECB_ECOLI", true);
		MAP_SOLUBLE.put("SELW_MOUSE", true);
		MAP_SOLUBLE.put("SERC_ECOLI", true);
		MAP_SOLUBLE.put("SH21A_HUMAN", true);
		MAP_SOLUBLE.put("SH3L3_HUMAN", true);
		MAP_SOLUBLE.put("SHE2_YEAST", true);
		MAP_SOLUBLE.put("SICP_SALTY", true);
		MAP_SOLUBLE.put("SIGE_SALTY", true);
		MAP_SOLUBLE.put("SKP1_YEAST", true);
		MAP_SOLUBLE.put("SLD5_HUMAN", true);
		MAP_SOLUBLE.put("SLYD_ECOLI", true);
		MAP_SOLUBLE.put("SMYD3_HUMAN", true);
		MAP_SOLUBLE.put("SNPA_STRCS", true);
		MAP_SOLUBLE.put("SODC_BOMMO", true);
		MAP_SOLUBLE.put("SODM_BACHA", true);
		MAP_SOLUBLE.put("SODN_STRCO", true);
		MAP_SOLUBLE.put("SP0F_BACSU", true);
		MAP_SOLUBLE.put("SPX_STRMU", true);
		MAP_SOLUBLE.put("SR3A_PHYPO", true);
		MAP_SOLUBLE.put("SRFAD_BACSU", true);
		MAP_SOLUBLE.put("SRP09_HUMAN", true);
		MAP_SOLUBLE.put("SRP14_MOUSE", true);
		MAP_SOLUBLE.put("SRP19_METJA", true);
		MAP_SOLUBLE.put("SRP19_PYRFU", true);
		MAP_SOLUBLE.put("SRP19_SULSO", true);
		MAP_SOLUBLE.put("SRXN1_HUMAN", true);
		MAP_SOLUBLE.put("SSPC_STAAW", true);
		MAP_SOLUBLE.put("SSRP_AQUAE", true);
		MAP_SOLUBLE.put("SSU72_HUMAN", true);
		MAP_SOLUBLE.put("ST1C3_HUMAN", true);
		MAP_SOLUBLE.put("SUBB_BACLE", true);
		MAP_SOLUBLE.put("SUFE_SALTY", true);
		MAP_SOLUBLE.put("SUMO3_HUMAN", true);
		MAP_SOLUBLE.put("SURE_AQUAE", true);
		MAP_SOLUBLE.put("SYH_ECOLI", true);
		MAP_SOLUBLE.put("SYW_MYCPN", true);
		MAP_SOLUBLE.put("T2AG_YEAST", true);
		MAP_SOLUBLE.put("TAGD_BACSU", true);
		MAP_SOLUBLE.put("TAT_HV1MA", true);
		MAP_SOLUBLE.put("TBCA_ARATH", true);
		MAP_SOLUBLE.put("TCL1A_HUMAN", true);
		MAP_SOLUBLE.put("TCTP_CAEEL", true);
		MAP_SOLUBLE.put("TEHB_HAEIN", true);
		MAP_SOLUBLE.put("TELT_HUMAN", true);
		MAP_SOLUBLE.put("TFL1_ARATH", true);
		MAP_SOLUBLE.put("THBI_RHOPR", true);
		MAP_SOLUBLE.put("THGA_ECOLI", true);
		MAP_SOLUBLE.put("THIO_METJA", true);
		MAP_SOLUBLE.put("TIMP1_HUMAN", true);
		MAP_SOLUBLE.put("TL5A_TACTR", true);
		MAP_SOLUBLE.put("TLP_PRUAV", true);
		MAP_SOLUBLE.put("TMT1_YEAST", true);
		MAP_SOLUBLE.put("TPIS_METJA", true);
		MAP_SOLUBLE.put("TPMT_HUMAN", true);
		MAP_SOLUBLE.put("TPPC2_MOUSE", true);
		MAP_SOLUBLE.put("TPPP3_HUMAN", true);
		MAP_SOLUBLE.put("TR112_YEAST", true);
		MAP_SOLUBLE.put("TRAM8_ECOLX", true);
		MAP_SOLUBLE.put("TRIA_TRIPA", true);
		MAP_SOLUBLE.put("TRM56_ARCFU", true);
		MAP_SOLUBLE.put("TRMD_HAEIN", true);
		MAP_SOLUBLE.put("TRMH_AQUAE", true);
		MAP_SOLUBLE.put("TRML_HAEIN", true);
		MAP_SOLUBLE.put("TRPR_ECOLI", true);
		MAP_SOLUBLE.put("TRXB1_ARATH", true);
		MAP_SOLUBLE.put("TRXH1_ARATH", true);
		MAP_SOLUBLE.put("TSN_HUMAN", true);
		MAP_SOLUBLE.put("TTHY_RAT", true);
		MAP_SOLUBLE.put("TUSA_ECO57", true);
		MAP_SOLUBLE.put("TUSB_ECOLI", true);
		MAP_SOLUBLE.put("TUSD_ECOLI", true);
		MAP_SOLUBLE.put("TUS_ECOLI", true);
		MAP_SOLUBLE.put("TXD17_HUMAN", true);
		MAP_SOLUBLE.put("TXFA2_DENAN", true);
		MAP_SOLUBLE.put("TYSY1_BACSU", true);
		MAP_SOLUBLE.put("TYW2_METJA", true);
		MAP_SOLUBLE.put("UB2D1_HUMAN", true);
		MAP_SOLUBLE.put("UBL5_HUMAN", true);
		MAP_SOLUBLE.put("UDP_ECOLI", true);
		MAP_SOLUBLE.put("UFM1_MOUSE", true);
		MAP_SOLUBLE.put("UK114_HUMAN", true);
		MAP_SOLUBLE.put("UNG_VIBCH", true);
		MAP_SOLUBLE.put("URE2_BACPA", true);
		MAP_SOLUBLE.put("URE3_MYCTU", true);
		MAP_SOLUBLE.put("UREE_ENTAE", true);
		MAP_SOLUBLE.put("UREF_HELPY", true);
		MAP_SOLUBLE.put("URHG2_BACSU", true);
		MAP_SOLUBLE.put("URM1_MOUSE", true);
		MAP_SOLUBLE.put("USPA_HAEIN", true);
		MAP_SOLUBLE.put("UTER_RAT", true);
		MAP_SOLUBLE.put("VANX_ENTFC", true);
		MAP_SOLUBLE.put("VG26_BPP22", true);
		MAP_SOLUBLE.put("VMAC3_AGKAC", true);
		MAP_SOLUBLE.put("VP1_BFPYV", true);
		MAP_SOLUBLE.put("WAPN_NAJNG", true);
		MAP_SOLUBLE.put("XIP1_WHEAT", true);
		MAP_SOLUBLE.put("XYN3_ASPKA", true);
		MAP_SOLUBLE.put("YBHB_ECOLI", true);
		MAP_SOLUBLE.put("YEGS_ECOLI", true);
		MAP_SOLUBLE.put("YFBR_ECOLI", true);
		MAP_SOLUBLE.put("YGFZ_ECOLI", true);
		MAP_SOLUBLE.put("YHDH_ECOLI", true);
		MAP_SOLUBLE.put("YKUV_BACSU", true);
		MAP_SOLUBLE.put("YN034_YEAST", true);
		MAP_SOLUBLE.put("YNU0_YEAST", true);
		MAP_SOLUBLE.put("YNZC_BACSU", true);
		MAP_SOLUBLE.put("YP225_YEAST", true);
		MAP_SOLUBLE.put("YRB1_YEAST", true);
		MAP_SOLUBLE.put("ZAPA_PSEAE", true);
		MAP_SOLUBLE.put("ZAPB_ECOLI", true);



		for (Sequence sequence : trainingCases) {
			if (MAP_SOLUBLE.containsKey(sequence.getId())) {
				logger.warn("SKIP TRAINING OF SOLUBLE "+sequence.getId());
				continue;
			}
			for (SlidingWindow slidingWindow : sequence.getWindows()) {
				int check = slidingWindow.getWindowIndex() % (Constants.WINDOW_LENGTH - 1);
				if (check == 0) {
					windowNew = true;
				} else {
					windowNew = false;
				}
				logger.trace("slidingWindowIndex: " + slidingWindow.getWindowIndex() + " -> newWindow:" + windowNew + " (check value: " + check + ") --> " + Arrays.toString(slidingWindow.getSequence()));

				Vertex vertexMiddle = null;
				SequencePosition spMiddle = null;

				for (int i = 0; i < slidingWindow.getSequence().length - 1; i++) {
					SequencePosition spSource = slidingWindow.getSequence()[i];
					Vertex vertexSource = null;
					SequencePosition spTarget = slidingWindow.getSequence()[i + 1];
					Vertex vertexTarget = null;

					//source
					if (spSource == null) {
						continue;
					} else {
						String sourceAa = spSource.getAminoAcid().toString().intern();
						String sourceSse = spSource.getSecondaryStructure().toString().intern();
						Double sourceHp = round(spSource.getHydrophobicity());
						vertexSource = mapVertex.get(sourceAa + ":" + sourceSse + ":" + sourceHp);
					}

					if (i == middle) {
						//if source vertex == middle vertex
						vertexMiddle = vertexSource;
						spMiddle = spSource;
					}

					//target
					if (spTarget == null) {
						continue;
					} else {
						String targetAa = spTarget.getAminoAcid().toString().intern();
						String targetSse = spTarget.getSecondaryStructure().toString().intern();
						Double targetHp = round(spTarget.getHydrophobicity());
						vertexTarget = mapVertex.get(targetAa + ":" + targetSse + ":" + targetHp);
					}

					//link the source and target vertices
					checkEdge(vertexSource, spSource, vertexTarget, spTarget, false);

				}
				//link the middle node to the RealClass (OUTSIDE, INSIDE, TMH)
				logger.trace("SequencePosition: middle " + spMiddle);
				checkEdge(vertexMiddle, spMiddle, null, null, true);
			}
		}
		trained = true;
		long end = System.currentTimeMillis();
		logger.info("-> " + wintermute.edgeSet().size() + " edges in " + (end - start) + " ms");
//        norm = new Normalizer(wintermute);
//        norm.normalize();
//        normalizedMin = norm.getNormalizedMin();


		//DEBUG
//        try {
//            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("MARKOV_DEBUG.txt")));
//            for (Vertex vertex : wintermute.vertexSet()) {
//                bw.write(vertex.toString() + "\n");
//            }
//            bw.write("\n\n\n\n\n");
//            for (Edge edge : wintermute.edgeSet()) {
//                bw.write(edge.toString() + "\n");
//            }
//            bw.flush();
//            bw.close();
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(Markov.class.getName()).log(Level.SEVERE, null, ex);
//        }
	}

	@Override
	public void save(File model) throws Exception {
		if (!trained) {
			throw new VerifyError("Can not save an empty model! Train it before!");
		}
		long start = System.currentTimeMillis();

		logger.info("saving " + model.getAbsolutePath() + " (v: " + wintermute.vertexSet().size()
				+ " | e: " + wintermute.edgeSet().size() + ")");
		BufferedWriter bw = new BufferedWriter(new FileWriter(model));
		GraphMLExporter g = new GraphMLExporter(new MarkovVertexNameProvider(), null, new MarkovEdgeNameProvider(), null);
		g.export(bw, wintermute);

		{
			//verify
			bw.write("<!-- vertex=aa(enum):sse(enum):hp(Double) -->\n");
			bw.write("<!-- edge_id=weight(double):weightTmh(double):weightNonTmh(double) | edge_source/target vertex=@vertex -->\n");
			bw.write("<!-- normalizedMin:" + normalizedMin + " -->\n");
			bw.write("<!-- wintermute:" + ((double) wintermute.edgeSet().size() / (double) wintermute.vertexSet().size()) + " -->");
		}

		bw.flush();
		bw.close();
		long end = System.currentTimeMillis();
		logger.info("-> in " + (end - start) + " ms (" + (model.length() / 1024) + " kb)");
	}

	@Override
	public void load(File model) throws Exception {
		if (trained) {
			throw new VerifyError("Model can not be overloaded! Create new emtpy Instance of markov!");
		}

		long start = System.currentTimeMillis();
		logger.info("reading " + model.getAbsolutePath() + " (" + (model.length() / 1024) + " kb)");

		GraphXmlHandler graphXmlHandler = new GraphXmlHandler();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		factory.setXIncludeAware(true);

		SAXParser parser = factory.newSAXParser();
		parser.parse(model, graphXmlHandler);

		logger.info("adding " + graphXmlHandler.getListVertex().size() + " vertices and " + graphXmlHandler.getListEdge().size() + " edges");
		for (String edgeConfig : graphXmlHandler.getListEdge()) {
			String[] parts = edgeConfig.split(";"); //source;target;weight

			//source
			String[] src = parts[0].split(":"); //source=aa:sse:hp
			String aa = src[0].intern();
			String sse = src[1].intern();
			double hp = Double.parseDouble(src[2].intern());
			Vertex source = new Vertex(aa, sse, hp);

			//target
			String[] trg = parts[1].split(":"); //target=aa:sse:hp
			aa = trg[0].intern();
			sse = trg[1].intern();
			hp = Double.parseDouble(trg[2].intern());
			Vertex target = new Vertex(aa, sse, hp);

			//add vertices, edge
			if (!wintermute.containsVertex(source)) {
				wintermute.addVertex(source);
			}
			if (!wintermute.containsVertex(target)) {
				wintermute.addVertex(target);
			}
			Edge edge = wintermute.addEdge(source, target);

			//edge
			String[] edgeId = parts[2].split(":"); //id=weight:weightTmh:weightNonTmh

			double weight = Double.parseDouble(edgeId[0]);
			double weightTmh = Double.parseDouble(edgeId[1]);
			double weightNonTmh = Double.parseDouble(edgeId[2]);
			{
				//verify weight
				if (weight != (weightTmh + weightNonTmh)) {
					throw new VerifyError("Edge is corrupted and can not be set! Export new model!"
							+ "\nedge: " + edge);
				}
			}
			wintermute.setEdgeWeight(edge, weight);
			edge.setWeight(true, weightTmh);
			edge.setWeight(false, weightNonTmh);
		}

		//missing vertices, which have no edges
		for (String vertex : graphXmlHandler.getListVertex()) {
			String[] parts = vertex.split(":");
			String aa = parts[0].intern();
			String sse = parts[1].intern();
			Double hp = Double.valueOf(parts[2].intern());
			Vertex tmp = new Vertex(aa, sse, hp);
			if (!wintermute.containsVertex(tmp)) {
				wintermute.addVertex(tmp);
			}
		}

		{
			//verify
			String shc = tail(model);
			if (shc.startsWith("<!-- ")) {
				String[] split = shc.split(" ")[1].split(":");
				if (split[0].equals("wintermute")) {
					double old = Double.parseDouble(split[1]);
					double act = ((double) wintermute.edgeSet().size() / (double) wintermute.vertexSet().size());
					if (old == act) {
						logger.info("model is OK and not corrupted");
					} else {
						throw new VerifyError("Model is corrupted and can not be read! Export new model!"
								+ "\nvertexSet: " + wintermute.vertexSet().size() + " | edgeSet: " + wintermute.edgeSet().size()
								+ "\nACTUAL (new): " + act
								+ "\nSAVED (old): " + old);
					}
				}
			}
		}

		trained = true;
		long end = System.currentTimeMillis();
		logger.info("-> in " + (end - start) + " ms");

	}

	protected double round(double value) {
		double result = value * hpRoundingValue;
		result = Math.round(result);
		result = result / hpRoundingValue;
		return result;
	}

	protected void checkEdge(Vertex source, SequencePosition spSource, Vertex target, SequencePosition spTarget, boolean middle) {
		if (!windowNew) {
			return;
		}
		boolean tmh = false;

		Result result = spSource.getRealClass();
		if (result.equals(Result.TMH)) {
			tmh = true;
			if (middle) {
				target = TMH;
			}
		} else if (result.equals(Result.NON_TMH)) {
			tmh = false;
			if (middle) {
				target = NON_TMH;
			}
		} else if (result.equals(Result.INSIDE)) {
			tmh = false;
			if (middle) {
				target = NON_TMH;
			}
		} else if (result.equals(Result.OUTSIDE)) {
			tmh = false;
			if (middle) {
				target = NON_TMH;
			}
		} else {
			logger.fatal("WARNING: result '" + result + "' can not be mapped to a vertex");
		}

		Edge edge = wintermute.getEdge(source, target);

		if (edge == null) {
			if (!wintermute.containsVertex(source)) {
				logger.fatal("WARNING: vertex source NOT contained: " + source
						+ "\nsource: " + source + " | spSource: " + spSource + " | target: " + target + " | middle: " + middle);
			}
			if (!wintermute.containsVertex(target)) {
				logger.fatal("WARNING: vertex target NOT contained: " + target
						+ "\nsource: " + source + " | spSource: " + spSource + " | target: " + target + " | middle: " + middle);
			}
			edge = wintermute.addEdge(source, target);
			wintermute.setEdgeWeight(edge, 1);

			edge.setWeight(tmh, 1);
			logger.trace("EDGE:CREATED: " + edge);
		} else {
			wintermute.setEdgeWeight(edge, (wintermute.getEdgeWeight(edge) + 1));

			edge.setWeight(tmh, edge.getWeight(tmh) + 1);
			logger.trace("EDGE:PUSHED: " + edge);
		}
	}

	/**
	 * checks hpscale for being the same as trained with
	 *
	 * @param scale
	 * @throws VerifyError if scale has changed within same instance of class
	 */
	protected void checkScale(int scale) {
		if (hpscaleUsed == -1) {
			hpscaleUsed = scale;
		} else if (hpscaleUsed != scale) {
			throw new VerifyError("Hydrophobocity scale has changed! Create new Instance or use data with the same scale!");
		}
	}

	protected String tail(File file) throws FileNotFoundException, IOException {
		RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
		long fileLength = file.length() - 1;
		StringBuilder sb = new StringBuilder();

		for (long filePointer = fileLength; filePointer != -1; filePointer--) {
			fileHandler.seek(filePointer);
			int readByte = fileHandler.readByte();

			if (readByte == 0xA) {
				if (filePointer == fileLength) {
					continue;
				} else {
					break;
				}
			} else if (readByte == 0xD) {
				if (filePointer == fileLength - 1) {
					continue;
				} else {
					break;
				}
			}

			sb.append((char) readByte);
		}

		String lastLine = sb.reverse().toString();
		return lastLine;
	}

	public void setMappingContValuesToNodes(double range) {
		hpSteppingValue = range;
		hpRoundingValue = 1 / range;
	}

	public double getHpSteppingValue() {
		return hpSteppingValue;
	}

	public MarkovDirectedWeightedGraph<Vertex, Edge> getGraph() {
		return wintermute;
	}

	public Vertex getVertexReference(String id) {
		if (mapVertex.containsKey(id)) {
			return mapVertex.get(id);
		} else {
			return null;
		}
	}
}
