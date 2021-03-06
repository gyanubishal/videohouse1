/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs544.videohouse.controller;

import cs544.videohouse.BCrypt;
import cs544.videohouse.domain.User;
import cs544.videohouse.domain.Video;
import cs544.videohouse.service.IUserService;
import cs544.videohouse.service.VideoService;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author GMaharjan
 */
@Controller
@SessionAttributes("loginUser")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private VideoService videoService;

    @RequestMapping("/")
    public String redirectRoot() {
        return "redirect:/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String redirectlogin(Model model) {
        System.out.println("redirect to login page");
        model.addAttribute("user", new User());
        return "login";
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String redirectRegistration(Model model) {
        System.out.println("GET method redirect to register");
        model.addAttribute("user", new User());
        return "registration";
    }

    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
 
    public String checkUserLogin(@Valid User user, BindingResult result, Model model) {
        String view = "login";
        if (result.hasErrors()) {
            if (user.getEmail().equals("") || user.getPassword().equals("")) {
                System.out.println("Result has error");
                return view;
            }
            User user_actual = userService.getUser(user.getEmail());
            if (user_actual == null) {
                System.out.println("No such User!!");
                return view;
            }
            if (BCrypt.checkpw(user.getPassword(), user_actual.getPassword())) {
                System.out.println("redirect to video page");
                model.addAttribute("loginUser", user_actual.getFirstName()+" "+user_actual.getLastName() );
                view = "redirect:/search";
            } else {
                System.out.println("redirect to login page");
            }
            return view;
        }
        return view;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String checkRegistration(@Valid User user, BindingResult result, Model model,final RedirectAttributes redirectAttributes) {
        String view = "registration";
        if (result.hasErrors()) {
            System.out.println("redirect to registration page (meth:POST)");
            return view;
        } else {
            String password = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(5));
            user.setPassword(password);
            userService.createUser(user);
            System.out.println("redirect to login page");
            //model.addAttribute("user", user);
            redirectAttributes.addFlashAttribute("registerMessage", user.getFirstName()+" "+user.getLastName()+" successfully registered.");
            view = "redirect:/login";
            return view;
        }
    }

    @RequestMapping(value = "/video", params = {"id"}, method = RequestMethod.GET)
    public String showVideoPage(@RequestParam(value = "id") String id, Model model) {
        Video v = videoService.getVideo(Long.valueOf(id));
        v.setViewCount(v.getViewCount() + 1);
        videoService.saveVideo(v);
        model.addAttribute("video", v);
        return "video";
    }

    @RequestMapping(value = "/search", params = {"query"}, method = RequestMethod.POST)
    public String searchVideo(@RequestParam(value = "query") String search, Model model) {
        List<Video> videoList = null;
        System.out.println("search : "+search);
        if (search.isEmpty()) {
            videoList = videoService.getVideos();
        } else {
            videoList = videoService.getVideosForSearch(search);
        }

        model.addAttribute("videoList", videoList);
        model.addAttribute("query", search);
        System.out.println("redirect to search page");
        return "search";
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String watchVideo(Model model) {
        List<Video> videoList = videoService.getVideos();
        model.addAttribute("videoList", videoList);
        System.out.println("redirect to search page");
        return "search";
    }
}
