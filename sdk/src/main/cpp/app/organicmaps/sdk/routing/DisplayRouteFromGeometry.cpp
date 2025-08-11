#include "RoutingManager.hpp"
#include "drape_frontend/route_shape.hpp"
#include "drape/pointers.hpp"
#include "geometry/point_with_altitude.hpp"

#include <mutex>
#include <vector>
#include <utility>

using namespace std;

void RoutingManager::DisplayRouteFromGeometry(std::vector<geometry::PointWithAltitude> const & points)
{
  if (points.size() < 2)
    return;

  auto subroute = make_unique_dp<df::Subroute>();
  subroute->m_routeType = df::RouteType::Car;

  std::vector<m2::PointD> poly;
  poly.reserve(points.size());
  for (auto const & p : points)
    poly.emplace_back(p.GetPoint());
  subroute->m_polyline = m2::PolylineD(std::move(poly));
  subroute->AddStyle(df::SubrouteStyle(df::kRouteColor, df::kRouteOutlineColor));

  auto const id =
      m_drapeEngine.SafeCallWithResult(&df::DrapeEngine::AddSubroute,
                                       df::SubrouteConstPtr(subroute.release()));
  lock_guard<mutex> guard(m_drapeSubroutesMutex);
  m_drapeSubroutes.push_back(id);
}

