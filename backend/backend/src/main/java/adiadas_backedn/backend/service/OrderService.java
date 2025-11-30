package adiadas_backedn.backend.service;

import adiadas_backedn.backend.dto.OrderItemRequest;
import adiadas_backedn.backend.dto.OrderRequest;
import adiadas_backedn.backend.model.Order;
import adiadas_backedn.backend.model.OrderItem;
import adiadas_backedn.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrder(OrderRequest request) {
        Order order = new Order();

        // Tạo ID ngẫu nhiên
        order.setId("ORD-" + UUID.randomUUID().toString().substring(0, 8));

        order.setCustomerName(request.getFullName());
        order.setEmail(request.getEmail());
        order.setPhoneNumber(request.getPhone());
        order.setAddress(request.getAddress());
        order.setNotes(request.getNotes());
        order.setOrderValue(request.getTotal());
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        order.setUserId(request.getUserId());

        // Tạo list OrderItem
        List<OrderItem> orderItems = new ArrayList<>();

        if (request.getItems() != null) {
            for (OrderItemRequest itemReq : request.getItems()) {
                OrderItem item = new OrderItem();

                item.setProductId(itemReq.getProductId());
                item.setProductName("Sản phẩm " + itemReq.getProductId());
                item.setQuantity(itemReq.getQuantity());
                item.setPrice(itemReq.getPrice());
                item.setColorName(itemReq.getColorName());
                item.setSizeValue(itemReq.getSizeValue());

                // Gán order cha đúng cách
                item.setOrder(order);

                orderItems.add(item);
            }
        }

        order.setOrderItems(orderItems);

        return orderRepository.save(order);
    }
    // trong OrderService
    public Order save(Order order) {
        return orderRepository.save(order);
    }

}
