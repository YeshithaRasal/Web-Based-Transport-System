package lk.swiftgolanka.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFound(ResourceNotFoundException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", 404);
        mav.addObject("error", "Not Found");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ModelAndView handleInvalidOperation(InvalidOperationException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", 400);
        mav.addObject("error", "Bad Request");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ModelAndView handleUnauthorized(UnauthorizedException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", 403);
        mav.addObject("error", "Access Denied");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", 500);
        mav.addObject("error", "Internal Server Error");
        mav.addObject("message", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.");
        return mav;
    }
}
