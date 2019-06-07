package in.zaidi.spring.session.redis.example.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import in.zaidi.spring.session.redis.example.dto.AnyEmbeddedObject;
import in.zaidi.spring.session.redis.example.dto.SessionScopedDTO;

@Controller
public class IndexController {

	@Autowired
	private SessionScopedDTO sessionComponent;

	@GetMapping("/")
	public String index(HttpServletRequest request, Model model) {
		System.out.println("Scoped SessionComponent page views: " + sessionComponent.getPageViews());
		
		Integer pageViews = 1;
		AnyEmbeddedObject pt = null;
		if (request.getSession().getAttribute("pageViews") != null) {
			pageViews += (Integer) request.getSession().getAttribute("pageViews");
		}
		
		if (request.getSession().getAttribute("pt") != null) {
		    pt = ((AnyEmbeddedObject)request.getSession().getAttribute("pt"));
		    pt.setObjectId("updated with page view "+pageViews);
        }else {
            pt =  new AnyEmbeddedObject();
            pt.setObjectId("createdNew with page view "+pageViews);
        }

		sessionComponent.setPageViews(pageViews);
		sessionComponent.setPt(pt);
		request.getSession().setAttribute("pageViews", pageViews);
		request.getSession().setAttribute("pt", pt);

		model.addAttribute("pageViews", pageViews);
		model.addAttribute("pt", pt);
		return "index";
	}

}
