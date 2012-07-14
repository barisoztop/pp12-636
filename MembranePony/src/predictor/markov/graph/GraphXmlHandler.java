package predictor.markov.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author rgreil
 */
public class GraphXmlHandler extends DefaultHandler {

    private List<String> listVertex;
    private List<String> listEdge;

    @Override
    public void startDocument() {
        listVertex = new ArrayList<String>();
        listEdge = new ArrayList<String>();
    }

//    @Override
//    public void endDocument() {
//    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equals("node")) {
            listVertex.add(attributes.getValue("id"));
        } else if (qName.equals("edge")) {
            String id = attributes.getValue("id");
            String source = attributes.getValue("source");
            String target = attributes.getValue("target");
            listEdge.add(source+";"+target+";"+id);
        }
    }

//    @Override
//    public void endElement(String uri, String localName, String qName) {
//    }

    public List<String> getListVertex() {
        return Collections.unmodifiableList(listVertex);
    }

    public List<String> getListEdge() {
        return Collections.unmodifiableList(listEdge);
    }
}
