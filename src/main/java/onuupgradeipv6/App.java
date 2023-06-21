package onuupgradeipv6;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class App {

	public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        
        try {
        	// Abre un archivo de registro en modo de escritura
        	PrintStream logStream = new PrintStream(new FileOutputStream("archivo.log"));

            //USER INTERACTION--------------------------
            System.out.println("- Ingresa bloque IP. xxx.xxx.xxx. ");
            String ip = scanner.nextLine();
            System.out.print("Usuario: ");
            String user = scanner.nextLine();
            System.out.print("Password: ");
            String pass = scanner.nextLine();
            ArrayList<String> bloque = new ArrayList<>();
            Boolean showBrowser = false;
            System.out.println("Mostrar ventana de navegador? 1.Si/2.No");
            int opc = scanner.nextInt();
            
            if (opc == 1)
            {
            	showBrowser = false;
            }else
            {
            	showBrowser = true;
            } 
            bloque = generarIps(ip);
            
            scanner.close();
            //END INTERACTION
            
            
            // PLAYWRIGHT INIT
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(showBrowser).setChannel("chrome"));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true).setJavaScriptEnabled(true));
//            Page page = context.newPage();
//            page.onDialog(dialog -> dialog.accept());
            // Page page = browser.newPage(new Browser.NewPageOptions().setIgnoreHTTPSErrors(true));
            // permite que los dialog no se cierren automaticos y los acepta
            // PLAYWRIGHT END
            
           
            // Redirige la salida estándar a través de System.setOut()
//            System.setOut(logStream);

            Boolean onuCorrecta = false;
            Boolean onuCorrectav2 = false;
            int onuCount = 0;
            for(int i = 0; 2 <= 252; i++)
            {

            			ip = bloque.get(i);
            			System.out.println("- Intendando conexion con: " + ip.toString());
            			
                        Page page = context.newPage();
                        page.onDialog(dialog -> dialog.accept());
            			
            			page.setDefaultTimeout(4000);
            			onuCorrecta = onuv1(ip, page);
            			if (onuCorrecta)
            			{
            				page.setDefaultTimeout(10000);
            				System.out.println("- ONU v1.5 Detectada");
            				
            				if(login(ip, page, user, pass) == true)
            				{
            					if(updateStatus(ip, page) == false)//si la onu no esta actualizada la manda a actualizar
            					{
            						onuCount++;
            						 upgrated(ip, page);
            						 
//            						 Page newPage = context.waitForPage(() -> {
//            							 page.getByText("Nueva pestaña").click();
//            							});
//            						 newPage.waitForLoadState();
//            						newPage.close();
            						 
			                         login(ip, page, user, pass);
			                         updateStatus(ip, page);
//			                         page.setDefaultTimeout(10000);
			                         logout(ip, page);
            						page.close();
            					}else{
            						System.out.println("Systema ya actualizado");
        							page.close();
            					}
            				}else{
            					System.out.println("- Equipo dejo de responder....");
    							page.close();
            				}
            				
            				
            			}else{ /// METODO LOGIN 2
							page.close();
            				
            				System.out.println("- Login Metodo v2");
            				
            	            Page page2 = context.newPage();
            	            page2.onDialog(dialog -> dialog.accept());

            				
            				page2.setDefaultTimeout(4000);
            				onuCorrectav2 = onuv2(ip, page2);
            				
            				if(onuCorrectav2)
            				{
            					page2.setDefaultTimeout(10000);
            					System.out.println("- ONU v1.5 Detectada");
            					if(loginv2(ip, page2, user, pass) == true)
            					{
            						
            						if(updateStatus(ip, page2) == false)//si la onu no esta actualizada la manda a actualizar
            						{
            							onuCount++;
	        							upgrated(ip, page2);
	        							
//	            						 Page newPage2 = context.waitForPage(() -> {
//	            							 page.getByText("Nueva pestaña").click();
//	            							});
//	            						 newPage2.waitForLoadState();
//	            						newPage2.close();
	            						
			                            loginv2(ip, page2, user, pass);
			                            updateStatus(ip, page2);
			                            logout(ip, page2);
            							page2.close();
//			            				page2.setDefaultTimeout(10000);
            						}else{
            							System.out.println("Systema ya actualizado");
            							page2.close();
            						}
            					}else{
            						System.out.println("- Equipo dejo de responder....");
        							page2.close();
            					}
            				}// end if v2
            			}

//                context.close();
//                page.close();
//                browser.close();

            // Restaura la salida estándar
//            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

            // Cierra el archivo de registro
//            logStream.close();
            		
            		
            		System.out.println("Onus Encontradas:" + onuCount);
            } //End For
    
            
        } catch (Exception e) {
            System.out.println("Para mas informacion revisar el archivo .log");
        }  
        
        
        System.out.println("Scann Finalizado ");
	}//END MAIN
	
	public static Boolean onuv1(String ip, Page page)
    {
        Boolean onuv1 = false;
        //onu url
        try {
            page.navigate("http://" + ip + "/admin/login.asp");
            // page.setDefaultTimeout(3000);
            String name = page.locator("[name=username]").innerText();
            String pass = page.locator("[name=password]").innerText();
            String code = page.locator("[name=Create_Code]").innerText();
            String verify = page.locator("[name=verification_code]").innerText();
            // page.setDefaultTimeout(30000);

            if( name != null && pass != null && code != null && verify != null)
            {
                onuv1 = true;
            }else {
            	onuv1 = false;
            }
            

        } catch (Exception e) {
           System.out.println("- No se logro establecer la conexion");
           onuv1 = false;
           page.close();
        }

        return onuv1;
    }

    public static Boolean onuv2(String ip, Page page)
    {
        Boolean onuv2 = false;
        //onu url
        try {
            page.navigate("http://" + ip + "/admin/login.asp");
            String name = page.locator("[name=username]").innerText();
            String pass = page.locator("[name=password]").innerText();
            // String code = page.locator("[name=Create_Code]").innerText();
            // String verify = page.locator("[name=verification_code]").innerText();

            if( name != null && pass != null)
            {
                onuv2 = true ;
            }else
            {
            	onuv2 = false;
            }
            
        } catch (Exception e) {
            System.out.println("- No se logro establecer la conexion");
            onuv2 = false;
        	page.close();
        }

        return onuv2;

    }

    public static Boolean onuv3(String ip, Page page)
    {
        Boolean onuv3 = false;
        //onu url
        try {
            page.navigate("http://" + ip + "/admin/login.asp");
            String name = page.locator("[name=username1]").innerText();
            String pass = page.locator("[name=psd1]").innerText();
            String code = page.locator("[name=Create_Code]").innerText();
            String verify = page.locator("[name=verification_code]").innerText();

            if( name != null && pass != null && code != null && verify != null)
            {
              onuv3 = true;
            }else
            {
            	page.close();
            }
            
        } catch (Exception e) {
        	page.close();
        }

        return onuv3;

    }

    
    public static Boolean login(String ip, Page page, String user, String pass)
    {
        Boolean login = false;
        try {
        //onu url
        page.navigate("http://" + ip + "/admin/login.asp");
        // Text input login
        page.locator("[name=username]").fill(user);
        page.locator("[name=password]").fill(pass);

        // capchat pass
        String key = page.locator("[name=Create_Code]").inputValue();
        page.locator("[name=verification_code]").fill(key);
        page.locator("[name=save]").click();
        System.out.println("- Login Success");
        login = true;

        } catch (Exception e) {
            System.out.println("- Login fail");
            login = false;
        }

        return login;
    }
    
    public static Boolean loginv2(String ip, Page page, String user, String pass)
    {
        Boolean login = false;
        try {
        //onu url
        page.navigate("http://" + ip + "/admin/login.asp");
        // Text input login
        page.locator("[name=username]").fill(user);
        page.locator("[name=password]").fill(pass);

        page.locator("[name=save]").click();
        System.out.println("- Login Success");
        login = true;

        } catch (Exception e) {
            System.out.println("- Login fail");
            login = false;
        }

        return login;
    }

    public static void logout(String ip, Page page)
    {
        try {
            //logout page
            page.navigate("http://"+ ip +"/admin/logout.asp");
            page.locator("body > blockquote > form > input:nth-child(3)").click();
            System.out.println("- Logout");
            
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static void upgrated(String ip, Page page)
    {
    try {

        page.navigate("http://" + ip + "/upgrade.asp");

        dormir(3000);
        FileChooser fileChooser = page.waitForFileChooser(() -> {
            page.locator(("[name=binary]")).click();
        });

        //directory update
        fileChooser.setFiles(Paths.get("C:\\FW_UPDATE\\V2802RGWT_COMPU_V1.9.1.6-230530_S10407.tar"));
        page.locator("[name=send]").click();

        //3 minutos de espera
        double porcentaje = 0;
        DecimalFormat df = new DecimalFormat("#.00");
        
        for(double i = 1; i < 18; i++)
        {
            // limpiarConsola(); 
            porcentaje = (i / 18) * 100; //tiempo en segundos
            System.out.print(df.format(porcentaje)+ "% ");
            dormir(10000);//10 SEGUNDOS ESPERA POR CADA 1 SEGUNDO DEL CONTADOR
        }
        
        System.out.println("");
        	
            } catch (Exception e) {
                System.out.println("- No se pudo enviar al binario.tar");
            }
    

    }
    
    public static Boolean updateStatus(String ip, Page page)
    {
        Boolean actualizada = false;
        String ipaddress = "";
        String upTime = "";
        String version = "";
        String macAddress = "";
        
        try {
            page.navigate("http://" + ip + "/status.asp");
            upTime = page.locator("form:nth-child(3) > table:nth-child(1) > tbody > tr:nth-child(3) > td:nth-child(2) > font").innerText();
            version = page.locator("table:nth-child(1) > tbody > tr:nth-child(4) > td:nth-child(2) > font").innerText();
            macAddress = page.locator("table:nth-child(2) > tbody > tr:nth-child(5) > td:nth-child(2) > font").innerText();
            ipaddress = page.locator("table > tbody > tr:nth-child(3) > td:nth-child(6) > font").innerText();
            
            if (version.contains("V1.9.1.6-230530"))
            {
                System.out.println("--------------------------------");
                System.out.println("***System Updated IPV6***");
                System.out.println("Fw: "+ version);
                System.out.println("Mac: "+macAddress);
                System.out.println("Ip: "+ipaddress);
                System.out.println("UpTime: " + upTime);
                System.out.println("--------------------------------");
                return actualizada = true;
            }else
            {
                System.out.println("--------------------------------");
                System.out.println("***System NOT Updated***");
                System.out.println("Fw: "+ version);
                System.out.println("Mac: "+macAddress);
                System.out.println("Ip: "+ipaddress);
                System.out.println("UpTime: " + upTime);
                System.out.println("--------------------------------");
                return actualizada = false;
    
            } 
        } catch (Exception e) {
                
        }
        return actualizada;

    }

    public static void limpiarConsola() {  
    System.out.print("\033[H\033[2J");  
    System.out.flush();  
}  
    
    public static void dormir(int x)
    {
        try {
            Thread.sleep(x);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public static ArrayList<String> generarIps(String ip)
    {
        ArrayList<String> bloque = new ArrayList<>();
        for(int i = 2; i<= 254; i++)
        {
            bloque.add(ip+i);
        }
        return bloque;
    }

   
    
}//END CLASS
