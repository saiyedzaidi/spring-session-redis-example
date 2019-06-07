package in.zaidi.spring.session.redis.example.dto;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionScopedDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer pageViews;
	
	private AnyEmbeddedObject pt;

	public AnyEmbeddedObject getPt() {
        return pt;
    }

    public void setPt(AnyEmbeddedObject pt) {
        this.pt = pt;
    }

    public void setPageViews(Integer pageViews) {
		this.pageViews = pageViews;
	}

	public Integer getPageViews() {
		return pageViews;
	}

}
