import { G, Line, Rect, Set, Title } from '../tags';
import { toDate, isAfter, isBefore } from '../utils';
import Band from '../core/Band';
import Timeline from '../Timeline';
import { EventEvent } from '../events';
import RenderContext from '../RenderContext';
import { Action } from '../Action';

/**
 * Day/Night terminator band
 * Indicates sun visibility.
 */
export default class DayNightBand extends Band {

  static get type() {
    return 'DayNightBand';
  }

  static get rules() {
    return {
      dayColor: '#fff',
      nightColor: '#000',
      cursor: 'pointer',
      dayHoverColor: '#e0e0e0',
      nightHoverColor: 'grey',
      dividerColor: '#aaa',
      dark: {
        dayColor: '#bbb',
      },
    };
  }

  hatchUncovered: boolean;
  interactiveDays: boolean;
  events: any[];

  private eventsById: { [key: string]: object } = {};

  constructor(timeline: Timeline, opts: any, style: any) {
    super(timeline, opts, style);

    this.hatchUncovered = (opts.hatchUncovered !== false);
    this.interactive = (opts.interactive === true);
    this.interactiveDays = (opts.interactiveDays === true);
    this.events = opts.events || [];
  }

  renderViewport(ctx: RenderContext) {
    const g = new G();

    if (this.hatchUncovered) {
      g.addChild(new Rect({
        x: ctx.x + this.timeline.positionDate(this.timeline.loadStart),
        y: ctx.y,
        width: this.timeline.pointsBetween(this.timeline.loadStart, this.timeline.loadStop),
        height: this.height,
        fill: this.style['hatchFill'],
        'pointer-events': 'none',
      }));
    }

    for (let idx = 0; idx < this.events.length; idx++) {
      const event = this.events[idx];
      const start = toDate(event.start);
      const stop = toDate(event.stop);
      const id = Timeline.nextId();

      if (isBefore(start, this.timeline.loadStop) && isAfter(stop, this.timeline.loadStart)) {
        const bgRect = new Rect({
          id,
          x: ctx.x + this.timeline.positionDate(start),
          y: ctx.y,
          width: this.timeline.pointsBetween(start, stop),
          height: this.height,
          fill: (event.day ? this.style['dayColor'] : this.style['nightColor']),
        });
        if (event.tooltip) {
          bgRect.addChild(new Title({}, event.tooltip));
        }
        g.addChild(bgRect);

        if (this.interactive) {
          if (!event.day || this.interactiveDays) {
            bgRect.setAttribute('cursor', this.style['cursor']);
            bgRect.addChild(new Set({
              attributeName: 'fill',
              to: (event.day ? this.style['dayHoverColor'] : this.style['nightHoverColor']),
              begin: 'mouseover',
              end: 'mouseout',
            }));
            this.eventsById[id] = event;
            this.timeline.registerInteractionTarget(id);
          }
        }

        // Vertical start divider
        if (!isBefore(start, this.timeline.loadStart)) {
          g.addChild(new Line({
            x1: ctx.x + this.timeline.positionDate(start),
            y1: ctx.y,
            x2: ctx.x + this.timeline.positionDate(start),
            y2: ctx.y + this.height,
            stroke: this.style['dividerColor'],
          }));
        }

        // Only draw stop divider if this is the last period
        if (idx === this.events.length - 1 && isBefore(stop, this.timeline.loadStop)) {
          g.addChild(new Line({
            x1: ctx.x + this.timeline.positionDate(stop),
            y1: ctx.y,
            x2: ctx.x + this.timeline.positionDate(stop),
            y2: ctx.y + this.height,
            stroke: this.style['dividerColor'],
          }));
        }
      }
    }
    return g;
  }

  onAction(id: string, action: Action) {
    super.onAction(id, action);
    if (this.eventsById[id]) {
      switch (action.type) {
      case 'click':
        const eventClickEvent = new EventEvent(this.eventsById[id], action.target);
        eventClickEvent.clientX = action.clientX;
        eventClickEvent.clientY = action.clientY;
        this.timeline.fireEvent('eventClick', eventClickEvent);
        break;
      case 'contextmenu':
        const eventContextMenuEvent = new EventEvent(this.eventsById[id], action.target);
        eventContextMenuEvent.clientX = action.clientX;
        eventContextMenuEvent.clientY = action.clientY;
        this.timeline.fireEvent('eventContextMenu', eventContextMenuEvent);
        break;
      case 'mouseenter':
        const mouseEnterEvent = new EventEvent(this.eventsById[id], action.target);
        mouseEnterEvent.clientX = action.clientX;
        mouseEnterEvent.clientY = action.clientY;
        this.timeline.fireEvent('eventMouseEnter', mouseEnterEvent);
        break;
      case 'mousemove':
        const mouseMoveEvent = new EventEvent(this.eventsById[id], action.target);
        mouseMoveEvent.clientX = action.clientX;
        mouseMoveEvent.clientY = action.clientY;
        this.timeline.fireEvent('eventMouseMove', mouseMoveEvent);
        break;
      case 'mouseleave':
        const mouseLeaveEvent = new EventEvent(this.eventsById[id], action.target);
        mouseLeaveEvent.clientX = action.clientX;
        mouseLeaveEvent.clientY = action.clientY;
        this.timeline.fireEvent('eventMouseLeave', mouseLeaveEvent);
        break;
      }
    }
  }
}
