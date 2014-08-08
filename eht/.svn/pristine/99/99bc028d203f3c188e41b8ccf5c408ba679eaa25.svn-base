package org.jeecgframework.core.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class VerifiCodeServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			response.setContentType("image/jpeg");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			HttpSession session = request.getSession();
			// 在内存中创建图象
			int width = 60, height = 17;
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			// 获取图形上下文
			Graphics g = image.getGraphics();
			// 生成随机类
			Random random = new Random();
			// 设定背景色
			int r1 = 200 + random.nextInt(250 - 200);
			int g1 = 200 + random.nextInt(250 - 200);
			int b1 = 200 + random.nextInt(250 - 200);
			g.setColor(new Color(r1, g1, b1));
			g.fillRect(0, 0, width, height);
			// 设定字体
			g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
			// 画边框
			g.setColor(new Color(0, 0, 0));
			g.drawRect(0, 0, width - 1, height - 1);
			// 随机产生155条干扰线，使图象中的认证码不易被其它程序探测到
			int r2 = 160 + random.nextInt(200 - 160);
			int g2 = 160 + random.nextInt(200 - 160);
			int b2 = 160 + random.nextInt(200 - 160);
			g.setColor(new Color(r2, g2, b2));
			for (int i = 0; i < 155; i++) {
				int x = random.nextInt(width);
				int y = random.nextInt(height);
				int xl = random.nextInt(12);
				int yl = random.nextInt(12);
				g.drawLine(x, y, x + xl, y + yl);
			}
			// 取随机产生的认证码(4位数字)
			String sRand = "";
			for (int i = 0; i < 4; i++) {
				String rand = String.valueOf(random.nextInt(10));
				sRand += rand;
				// 将认证码显示到图象中
				g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
				g.drawString(rand, 13 * i + 6, 16);
			}
			// 将认证码存入SESSION
			session.setAttribute("VerifiCode", sRand);
			// 图象生效
			g.dispose();
			ServletOutputStream responseOutputStream = response.getOutputStream();
			// 输出图象到页面
			ImageIO.write(image, "JPEG", responseOutputStream);
			// 以下关闭输入流！
			responseOutputStream.flush();
			responseOutputStream.close();
		} catch (Exception e) {
		}
	}

}
