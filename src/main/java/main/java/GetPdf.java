package main.java;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.awt.*;
import java.io.*;


@Path("ConvertService")
public class GetPdf {

    /** The SVG document factory. */
    protected static SAXSVGDocumentFactory factory;
    /** The SVG bridge context. */
    protected static BridgeContext ctx;
    /** The GVT builder */
    protected static GVTBuilder builder;
     public GetPdf(){
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        factory = new SAXSVGDocumentFactory(parser);
        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.DYNAMIC);
        builder = new GVTBuilder();
    }

    @POST
    @Path("/GetPdf")
    @Produces("application/pdf")
    public File GetPdf(@FormParam("jsonData") String jsonData) throws IOException {

        try {
            JSONArray jsonArray = JSONArray.fromObject(jsonData);

            Document document = new Document(new Rectangle(Float.parseFloat(jsonArray.getJSONObject(0).get("width").toString()),Float.parseFloat(jsonArray.getJSONObject(0).get("height").toString()) ));
            String tempDir = this.getClass().getResource("/").getFile().toString().replace("classes/","")+"resources/";
            tempDir = tempDir.substring(1,tempDir.length());
            File diretory = new File(tempDir);
            File tempFile = File.createTempFile("temp",".pdf",diretory);
            String tempPath = tempDir+tempFile.getName();

            PdfWriter  writer = PdfWriter.getInstance(document, new FileOutputStream(tempPath));
            document.open();
            FontFactory.register("");
            PdfContentByte cb = writer.getDirectContent();

            for (int i=0;i<jsonArray.size();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Float width = Float.parseFloat (jsonObject.get("width").toString()) ;
                Float height =Float.parseFloat( jsonObject.get("height").toString());
                PdfTemplate map = cb.createTemplate(width, height);
                File tempSvg =  File.createTempFile("temp",".svg",diretory);
                FileOutputStream fo = new FileOutputStream(tempSvg);
                String strSvg = jsonObject.get("svgString").toString();
                //strSvg = strSvg.replace("/uploads","file:/E:/site/onlineDesinge/uploads");
                fo.write(strSvg.getBytes("UTF-8"));
                fo.close();
                document.setPageSize(new Rectangle(width,height));
                drawSvg(map,tempSvg.toURI().toString(),width,height);
                cb.addTemplate(map, 0, 0);
                document.newPage();
            }
            // step 5
            document.close();
            writer.close();
            File file = new File(tempPath);
            return file;
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File("");
    }

    public void drawSvg(PdfTemplate map, String svgUrl,float width,float height) throws IOException {
        Graphics2D g2d = new PdfGraphics2D(map, width, height);
        SVGDocument svgDocument = factory.createSVGDocument(svgUrl);
        GraphicsNode mapGraphics = builder.build(ctx, svgDocument);
        mapGraphics.paint(g2d);
        g2d.dispose();
    }




}
